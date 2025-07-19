package com.albertoeg.android.tv.immich

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.leanback.app.BackgroundManager
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.FocusHighlight
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.VerticalGridPresenter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import arrow.core.Either
import com.albertoeg.android.tv.immich.api.ApiClient
import com.albertoeg.android.tv.immich.api.ApiClientConfig
import com.albertoeg.android.tv.immich.api.model.Asset
import com.albertoeg.android.tv.immich.card.Card
import com.albertoeg.android.tv.immich.card.CardPresenterSelector
import com.albertoeg.android.tv.immich.home.HomeFragmentDirections
import com.albertoeg.android.tv.immich.shared.prefs.API_KEY
import com.albertoeg.android.tv.immich.shared.prefs.DEBUG_MODE
import com.albertoeg.android.tv.immich.shared.prefs.DISABLE_SSL_VERIFICATION
import com.albertoeg.android.tv.immich.shared.prefs.HOST_NAME
import com.albertoeg.android.tv.immich.shared.prefs.LOAD_BACKGROUND_IMAGE
import com.albertoeg.android.tv.immich.shared.prefs.NAVIGATION_MODE
import com.albertoeg.android.tv.immich.shared.prefs.NavigationMode
import com.albertoeg.android.tv.immich.shared.prefs.PreferenceManager
import com.albertoeg.android.tv.immich.shared.util.Debouncer
import com.albertoeg.android.tv.immich.shared.viewmodel.KeyEventsViewModel
import com.albertoeg.tv.immich.shared.fragment.GridFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


abstract class VerticalCardGridFragment<ITEM> : GridFragment() {
    protected var assets: List<ITEM> = emptyList()
    protected val startPage = 1

    private lateinit var mMetrics: DisplayMetrics
    private var mBackgroundManager: BackgroundManager? = null

    protected lateinit var apiClient: ApiClient
    private lateinit var keyEvents: KeyEventsViewModel
    private val ZOOM_FACTOR = FocusHighlight.ZOOM_FACTOR_SMALL
    protected val ioScope = CoroutineScope(Job() + Dispatchers.IO)
    private val mainScope = CoroutineScope(Job() + Dispatchers.Main)
    private val assetsStillToRender: MutableList<ITEM> = mutableListOf()
    protected var currentPage: Int = startPage
    protected var allPagesLoaded: Boolean = false
    private var currentLoadingJob: Job? = null
    protected val selectionMode: Boolean
        get() = arguments?.getBoolean("selectionMode", false) ?: false
    private var currentSelectedIndex: Int = 0
    protected var currentNavigationMode: NavigationMode = NavigationMode.PHOTO_BY_PHOTO

    abstract fun sortItems(items: List<ITEM>): List<ITEM>
    abstract suspend fun loadItems(
        apiClient: ApiClient,
        page: Int,
        pageCount: Int
    ): Either<String, List<ITEM>>

    abstract fun createCard(a: ITEM): Card
    abstract fun getBackgroundPicture(it: ITEM): String?
    open fun setTitle(response: List<ITEM>) {
        // default no title
    }

    abstract fun onItemSelected(card: Card, indexOf: Int)
    abstract fun onItemClicked(card: Card)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (PreferenceManager.isLoggedId()) {
            apiClient =
                ApiClient.getClient(
                    ApiClientConfig(
                        PreferenceManager.get(HOST_NAME),
                        PreferenceManager.get(API_KEY),
                        PreferenceManager.get(DISABLE_SSL_VERIFICATION),
                        PreferenceManager.get(DEBUG_MODE)
                    )
                )
        } else {
            Toast.makeText(
                ImmichApplication.appContext,
                "Invalid Immich server settings, redirecting to login screen.",
                Toast.LENGTH_SHORT
            ).show()
            findNavController().navigate(HomeFragmentDirections.actionGlobalSignInFragment())
            return
        }

        keyEvents = ViewModelProvider(requireActivity())[KeyEventsViewModel::class.java]
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                keyEvents.state.collect {
                    // Handle special keys that don't need navigation mode consideration
                    if (it?.keyCode == KeyEvent.KEYCODE_FORWARD || it?.keyCode == KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD || it?.keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
                        updateManualPositionHandler(adapter.size() - 1)
                    } else if (it?.keyCode == KeyEvent.KEYCODE_MEDIA_REWIND || it?.keyCode == KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD) {
                        updateManualPositionHandler((currentSelectedIndex - FETCH_PAGE_COUNT).coerceAtLeast(0))
                    } else if (it?.keyCode == KeyEvent.KEYCODE_MENU) {
                        toggleNavigationMode()
                    }
                }
            }
        }

        setupAdapter()
        setupBackgroundManager()

        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            val card: Card = item as Card
            if (selectionMode) {
                card.selected = !card.selected
                adapter.notifyArrayItemRangeChanged(currentSelectedIndex, 1)
                onItemSelected(card, currentSelectedIndex)
            } else {
                onItemClicked(card)
            }
        }
        setOnItemViewSelectedListener { _, item, _, _ ->
            currentSelectedIndex = adapter.indexOf(item)
            item?.let {
                loadBackgroundDebounced((it as Card).backgroundUrl) {
                    loadBackgroundDebounced(it.thumbnailUrl) {
                        Timber.tag(javaClass.name)
                            .e("Could not load background url")
                    }
                }
                updateDateContext()
            }
            with(this@VerticalCardGridFragment) {
                loadNextItemsIfNeeded(adapter.indexOf(item))
            }
        }
        // fetch initial items
        fetchInitialItems()
    }

    private fun handleKeyEvent(event: KeyEvent?): Boolean {
        if (event?.action != KeyEvent.ACTION_DOWN) return false
        
        when (event.keyCode) {
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                // Check if we should open the popup menu instead
                if ((adapter.size() == 0 && allPagesLoaded) || currentSelectedIndex > 0 && (currentSelectedIndex % COLUMNS == 3 || currentSelectedIndex + 1 == adapter.size())) {
                    openPopUpMenu()
                    return true
                }
                return handleNavigationKeyEvent(event.keyCode)
            }
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                return handleNavigationKeyEvent(event.keyCode)
            }
        }
        return false // Let default handling occur for unhandled keys
    }

    private fun handleNavigationKeyEvent(keyCode: Int): Boolean {
        if (currentNavigationMode == NavigationMode.PHOTO_BY_PHOTO) {
            // Let default navigation handle it
            return false
        }

        Timber.d("Handling navigation key event: keyCode=$keyCode, mode=${currentNavigationMode.getTitle()}")
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_UP -> navigateByTimeMode(-1)
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_DOWN -> navigateByTimeMode(1)
            else -> return false
        }
        return true // Event was handled by time-based navigation
    }

    private fun navigateByTimeMode(direction: Int) {
        val currentAsset = getCurrentSelectedAsset() ?: return
        val targetIndex = findTargetIndexByNavigationMode(currentAsset, direction)
        Timber.d("Time-based navigation: direction=$direction, currentIndex=$currentSelectedIndex, targetIndex=$targetIndex, mode=${currentNavigationMode.getTitle()}")
        if (targetIndex != -1 && targetIndex != currentSelectedIndex && targetIndex < assets.size) {
            updateManualPositionHandler(targetIndex)
        }
    }

    private fun findTargetIndexByNavigationMode(currentAsset: ITEM, direction: Int): Int {
        val currentDate = getCurrentAssetDate(currentAsset) ?: return -1
        val calendar = Calendar.getInstance().apply { time = currentDate }

        when (currentNavigationMode) {
            NavigationMode.DAY_BY_DAY -> {
                calendar.add(Calendar.DAY_OF_YEAR, direction)
            }
            NavigationMode.WEEK_BY_WEEK -> {
                calendar.add(Calendar.WEEK_OF_YEAR, direction)
            }
            NavigationMode.MONTH_BY_MONTH -> {
                calendar.add(Calendar.MONTH, direction)
            }
            NavigationMode.YEAR_BY_YEAR -> {
                calendar.add(Calendar.YEAR, direction)
            }
            else -> return -1
        }

        val targetDate = calendar.time
        return findClosestAssetByDate(targetDate)
    }

    private fun findClosestAssetByDate(targetDate: Date): Int {
        var closestIndex = -1
        var minDifference = Long.MAX_VALUE

        for (i in assets.indices) {
            val assetDate = getCurrentAssetDate(assets[i]) ?: continue
            val difference = Math.abs(targetDate.time - assetDate.time)

            if (difference < minDifference) {
                minDifference = difference
                closestIndex = i
            }
        }

        return closestIndex
    }

    private fun getCurrentSelectedAsset(): ITEM? {
        return if (currentSelectedIndex in 0 until assets.size) assets[currentSelectedIndex] else null
    }

    private fun getCurrentAssetDate(asset: ITEM): Date? {
        // This needs to be implemented based on how ITEM provides date information
        // For now, assuming ITEM is Asset type based on GenericAssetFragment usage
        return if (asset is Asset) {
            asset.exifInfo?.dateTimeOriginal ?: asset.fileModifiedAt
        } else null
    }

    protected fun updateNavigationModeDisplay() {
        navigationModeValue?.text = currentNavigationMode.getTitle()
        updateDateContext()
    }

    private fun updateDateContext() {
        val currentAsset = getCurrentSelectedAsset()
        val dateText = when {
            currentAsset == null -> ""
            currentNavigationMode == NavigationMode.PHOTO_BY_PHOTO -> {
                formatPhotoDate(getCurrentAssetDate(currentAsset))
            }
            else -> formatTimeContextDate(getCurrentAssetDate(currentAsset))
        }
        currentDateContext?.text = dateText
    }

    private fun formatPhotoDate(date: Date?): String {
        if (date == null) return ""
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(date)
    }

    private fun formatTimeContextDate(date: Date?): String {
        if (date == null) return ""
        val calendar = Calendar.getInstance().apply { time = date }

        return when (currentNavigationMode) {
            NavigationMode.DAY_BY_DAY -> {
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
            }
            NavigationMode.WEEK_BY_WEEK -> {
                val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
                val year = calendar.get(Calendar.YEAR)
                "Week $weekOfYear, $year"
            }
            NavigationMode.MONTH_BY_MONTH -> {
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date)
            }
            NavigationMode.YEAR_BY_YEAR -> {
                calendar.get(Calendar.YEAR).toString()
            }
            else -> ""
        }
    }

    private fun toggleNavigationMode() {
        val modes = NavigationMode.entries
        val currentIndex = modes.indexOf(currentNavigationMode)
        val nextIndex = (currentIndex + 1) % modes.size
        val nextMode = modes[nextIndex]

        PreferenceManager.save(NAVIGATION_MODE, nextMode)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateNavigationModeDisplay()
        
        // Set key listener on the grid frame to handle navigation mode changes
        val gridFrame = view.findViewById<androidx.leanback.widget.BrowseFrameLayout>(R.id.grid_frame)
        gridFrame?.setOnKeyListener { _, _, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                handleKeyEvent(event)
            } else {
                false
            }
        }
    }

    protected open fun fetchInitialItems() {
        ioScope.launch {
            loadData().fold(
                { itLeft -> showErrorMessage(itLeft) },
                { itRight ->
                    val assets = filterItems(itRight)
                    setupViews(assets)
                    if (assets.size < FETCH_COUNT) {
                        // immediately load next assets
                        currentLoadingJob = fetchNextItems()
                    }
                }
            )
        }
    }

    private fun loadNextItemsIfNeeded(selectedIndex: Int) {
        if (selectedIndex != -1 && (adapter.size() - selectedIndex < FETCH_NEXT_THRESHOLD)) {
            if (currentLoadingJob?.isActive != true && assetsStillToRender.isEmpty() && !allPagesLoaded) {
                // try a next page if its available
                currentLoadingJob = fetchNextItems()
            } else {
                mainScope.launch {
                    addAssetsPaginated()
                }
            }
        }
    }

    protected open fun resortItems() {
        assets = sortItems(assets)
        adapter.clear()
        adapter.addAll(0, assets.map { createCard(it) })
    }

    protected open fun clearState() {
        currentPage = startPage
        assets = emptyList()
        assetsStillToRender.clear()
        adapter.clear()
    }

    protected open fun openPopUpMenu() {
        // to implement by children
    }

    private fun fetchNextItems(): Job {
        return ioScope.launch {
            val nextAssets = loadMoreAssets()
            setDataOnMain(nextAssets)
        }
    }

    protected open fun filterItems(items: List<ITEM>): List<ITEM> = items

    protected open suspend fun loadMoreAssets(): List<ITEM> {
        if (allPagesLoaded) {
            return emptyList()
        }
        currentPage += 1
        return loadData().fold(
            { errorMessage ->
                showErrorMessage(errorMessage)
                emptyList()
            },
            { items ->
                Timber.i("Loading next items, ${items.size}")
                val filteredItems = filterItems(items)
                allPagesLoaded = allPagesLoaded(items)
                if (filteredItems.size < FETCH_COUNT) {
                    return filteredItems + loadMoreAssets()
                } else  {
                    return filteredItems
                }
            }
        )
    }

    protected open fun allPagesLoaded(items: List<ITEM>): Boolean =
        items.size < FETCH_PAGE_COUNT

    override fun onResume() {
        super.onResume()
        if (assets.isNotEmpty()) {
            progressBar?.visibility = View.GONE
        }
    }

    private fun setupAdapter() {
        val presenter = VerticalGridPresenter(ZOOM_FACTOR)
        presenter.numberOfColumns = COLUMNS
        gridPresenter = presenter
        val cardPresenter = CardPresenterSelector(requireContext())
        adapter = ArrayObjectAdapter(cardPresenter)
    }

    private fun setupBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(activity)
        if (!mBackgroundManager!!.isAttached) {
            mBackgroundManager!!.attach(requireActivity().window)
        }
        mMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(mMetrics)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mBackgroundManager?.isAttached == true) {
            mBackgroundManager?.drawable = null
        }
    }

    private suspend fun setupViews(assets: List<ITEM>) =
        withContext(Dispatchers.Main) {
            progressBar?.visibility = View.GONE
            setTitle(assets)
            assets.firstOrNull()?.let { loadBackground(getBackgroundPicture(it)) {} }
            setDataOnMain(assets)
        }

    protected suspend fun setDataOnMain(assets: List<ITEM>) = withContext(Dispatchers.Main) {
        setData(assets)
    }

    protected open fun setData(assets: List<ITEM>) {
        val sortedItems = sortItems(assets.filter { !this@VerticalCardGridFragment.assets.contains(it) })
        this@VerticalCardGridFragment.assets += sortedItems
        assetsStillToRender.addAll(sortedItems)
        addAssetsPaginated()
    }

    protected open suspend fun loadData(): Either<String, List<ITEM>> {
        return loadItems(apiClient, currentPage, FETCH_PAGE_COUNT)
    }

    private fun loadBackgroundDebounced(backgroundUrl: String?, onLoadFailed: () -> Unit) {
        if (PreferenceManager.get(LOAD_BACKGROUND_IMAGE)) {
            Debouncer.debounce("background", { loadBackground(backgroundUrl, onLoadFailed) }, 1, TimeUnit.SECONDS)
        }
    }

    private fun loadBackground(backgroundUrl: String?, onLoadFailed: () -> Unit) {
        if (!isAdded || !PreferenceManager.get(LOAD_BACKGROUND_IMAGE)) {
            return
        }
        if (backgroundUrl.isNullOrEmpty()) {
            Timber.i("Could not load background because background url is null")
            return
        }
        val width: Int = mMetrics.widthPixels
        val height: Int = mMetrics.heightPixels
        try {
            Glide.with(requireActivity())
                .load(backgroundUrl)
                .centerCrop()
                .into(object : SimpleTarget<Drawable?>(width, height) {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable?>?
                    ) {
                        try {
                            mBackgroundManager?.drawable = resource
                        } catch (e: Exception) {
                            Timber.e(e, "Could not set background")
                        }
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        onLoadFailed()
                    }
                })
        } catch (e: Exception) {
            Timber.e(e, "Could not fetch background")
        }
    }

    private fun addAssetsPaginated() {
        val assetsPaginated = assetsStillToRender.take(FETCH_COUNT)
        val cards = assetsPaginated.map { createCard(it) }
        adapter.addAll(adapter.size(), cards)
        assetsStillToRender.removeAll(assetsPaginated)
    }

    private suspend fun showErrorMessage(message: String) = withContext(Dispatchers.Main) {
        if (isAdded) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
                .show()
        }
        Timber.e(message)
    }

    companion object {
        const val COLUMNS = 4
        private const val FETCH_NEXT_THRESHOLD = COLUMNS * 6
        const val FETCH_COUNT = 50
        const val FETCH_PAGE_COUNT = FETCH_COUNT
    }
}