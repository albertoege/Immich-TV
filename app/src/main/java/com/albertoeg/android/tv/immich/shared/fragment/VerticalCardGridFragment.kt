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

        // Cargar el modo de navegación desde las preferencias
        currentNavigationMode = PreferenceManager.get(NAVIGATION_MODE)

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
                keyEvents.state.collect { keyEvent ->
                    // Llamar a handleKeyEvent para procesar todos los eventos de teclas
                    handleKeyEvent(keyEvent)
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
                // Actualizar también la información de rango de fechas cuando cambies de foto
                updateDateRangeDisplay()
            }
            with(this@VerticalCardGridFragment) {
                loadNextItemsIfNeeded(adapter.indexOf(item))
            }
        }
        // fetch initial items
        fetchInitialItems()
    }

    private fun handleKeyEvent(event: KeyEvent?) {
        when (event?.keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_UP -> {
                if (currentNavigationMode != NavigationMode.PHOTO_BY_PHOTO) {
                    navigateByTimeMode(-1)
                }
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (currentNavigationMode != NavigationMode.PHOTO_BY_PHOTO) {
                    navigateByTimeMode(1)
                } else {
                    // Solo abrir menú si está en el borde derecho, no interferir con navegación normal
                    if ((adapter.size() == 0 && allPagesLoaded) || currentSelectedIndex > 0 && (currentSelectedIndex % COLUMNS == 3 || currentSelectedIndex + 1 == adapter.size())) {
                        openPopUpMenu()
                    }
                }
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (currentNavigationMode != NavigationMode.PHOTO_BY_PHOTO) {
                    navigateByTimeMode(1)
                }
            }
            KeyEvent.KEYCODE_FORWARD, KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD, KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                updateManualPositionHandler(adapter.size() - 1)
            }
            KeyEvent.KEYCODE_MEDIA_REWIND, KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD -> {
                updateManualPositionHandler((currentSelectedIndex - FETCH_PAGE_COUNT).coerceAtLeast(0))
            }
            KeyEvent.KEYCODE_MENU -> {
                // Toggle navigation mode display pero no interferir con navegación
                toggleNavigationMode()
            }
        }
    }

    private fun handleNavigationKeyEvent(keyCode: Int) {
        if (currentNavigationMode == NavigationMode.PHOTO_BY_PHOTO) {
            // Let default navigation handle it
            return
        }

        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_UP -> navigateByTimeMode(-1)
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_DOWN -> navigateByTimeMode(1)
        }
    }

    private fun navigateByTimeMode(direction: Int) {
        val currentAsset = getCurrentSelectedAsset() ?: return
        val currentDate = getCurrentAssetDate(currentAsset) ?: return
        val calendar = Calendar.getInstance().apply { time = currentDate }

        // Aplicar el cambio de tiempo según el modo actual
        when (currentNavigationMode) {
            NavigationMode.DAY_BY_DAY -> {
                calendar.add(Calendar.DAY_OF_YEAR, direction)
            }
            NavigationMode.WEEK_BY_WEEK -> {
                calendar.add(Calendar.WEEK_OF_YEAR, direction)
            }
            NavigationMode.MONTH_BY_MONTH -> {
                calendar.add(Calendar.MONTH, direction)
                // Para navegación por mes, reiniciar carga desde la fecha objetivo
                navigateToDateWithFreshLoad(calendar.time, currentNavigationMode, direction)
                return
            }
            NavigationMode.YEAR_BY_YEAR -> {
                calendar.add(Calendar.YEAR, direction)
                // Para navegación por año, reiniciar carga desde la fecha objetivo
                navigateToDateWithFreshLoad(calendar.time, currentNavigationMode, direction)
                return
            }
            else -> return
        }

        val targetDate = calendar.time
        val targetIndex = findBestAssetByDate(targetDate)

        if (targetIndex != -1 && targetIndex != currentSelectedIndex) {
            val foundAsset = assets[targetIndex]
            val foundDate = getCurrentAssetDate(foundAsset)

            if (foundDate != null) {
                updateManualPositionHandler(targetIndex)

                val periodName = when (currentNavigationMode) {
                    NavigationMode.MONTH_BY_MONTH -> if (direction > 0) "mes siguiente" else "mes anterior"
                    NavigationMode.YEAR_BY_YEAR -> if (direction > 0) "año siguiente" else "año anterior"
                    NavigationMode.WEEK_BY_WEEK -> if (direction > 0) "semana siguiente" else "semana anterior"
                    NavigationMode.DAY_BY_DAY -> if (direction > 0) "día siguiente" else "día anterior"
                    else -> "período"
                }

                Toast.makeText(
                    requireContext(),
                    "Navegando al $periodName: ${formatPhotoDate(foundDate)}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            // Para navegación por día/semana, usar lógica de búsqueda incremental
            if ((currentNavigationMode == NavigationMode.DAY_BY_DAY || currentNavigationMode == NavigationMode.WEEK_BY_WEEK) && !allPagesLoaded) {
                loadMoreDataAndSearchForDate(targetDate, direction)
            } else {
                val periodName = when (currentNavigationMode) {
                    NavigationMode.WEEK_BY_WEEK -> if (direction > 0) "semana siguiente" else "semana anterior"
                    NavigationMode.DAY_BY_DAY -> if (direction > 0) "día siguiente" else "día anterior"
                    else -> "período"
                }

                Toast.makeText(
                    requireContext(),
                    "No hay fotos disponibles para el $periodName",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Nueva función para navegar a una fecha específica con carga fresca de datos
    private fun navigateToDateWithFreshLoad(targetDate: Date, navigationMode: NavigationMode, direction: Int) {
        val calendar = Calendar.getInstance().apply { time = targetDate }
        val targetYear = calendar.get(Calendar.YEAR)
        val targetMonth = calendar.get(Calendar.MONTH)

        val periodName = when (navigationMode) {
            NavigationMode.MONTH_BY_MONTH -> {
                val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(targetDate)
                if (direction > 0) "mes siguiente ($monthName $targetYear)" else "mes anterior ($monthName $targetYear)"
            }
            NavigationMode.YEAR_BY_YEAR -> {
                if (direction > 0) "año siguiente ($targetYear)" else "año anterior ($targetYear)"
            }
            else -> "período"
        }

        // Mostrar mensaje de búsqueda
        Toast.makeText(
            requireContext(),
            "Buscando fotos del $periodName...",
            Toast.LENGTH_SHORT
        ).show()

        ioScope.launch {
            try {
                // Primero buscar en los datos ya cargados
                withContext(Dispatchers.Main) {
                    val quickIndex = when (navigationMode) {
                        NavigationMode.YEAR_BY_YEAR -> findAssetByYear(targetYear)
                        NavigationMode.MONTH_BY_MONTH -> findAssetByYearAndMonth(targetYear, targetMonth)
                        else -> findBestAssetByDate(targetDate)
                    }

                    if (quickIndex != -1) {
                        // Encontrado in datos existentes
                        val foundAsset = assets[quickIndex]
                        val foundDate = getCurrentAssetDate(foundAsset)

                        if (foundDate != null) {
                            updateManualPositionHandler(quickIndex)
                            Toast.makeText(
                                requireContext(),
                                "Navegando al $periodName: ${formatPhotoDate(foundDate)}",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@withContext
                        }
                    }
                }

                // Si no encontramos en datos existentes, cargar datos frescos desde la API
                val foundPhotos = searchPhotosFromDate(targetDate, navigationMode, direction)

                withContext(Dispatchers.Main) {
                    if (foundPhotos.isNotEmpty()) {
                        // Reiniciar la vista con las nuevas fotos encontradas
                        resetViewWithNewPhotos(foundPhotos, targetDate, periodName)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "No se encontraron fotos para el $periodName",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al buscar fotos para $periodName")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Error al buscar fotos para el $periodName",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Nueva función mejorada para buscar fotos desde una fecha específica usando la API
    private suspend fun searchPhotosFromDate(targetDate: Date, navigationMode: NavigationMode, direction: Int): List<ITEM> {
        val calendar = Calendar.getInstance().apply { time = targetDate }
        val targetYear = calendar.get(Calendar.YEAR)
        val targetMonth = calendar.get(Calendar.MONTH)

        Timber.d("Buscando fotos para: ${if (navigationMode == NavigationMode.YEAR_BY_YEAR) "año $targetYear" else "mes ${targetMonth + 1}/$targetYear"}")

        // Estrategia 1: Cargar muchas páginas para tener un rango amplio de fechas
        val allFoundItems = mutableListOf<ITEM>()
        var searchPage = 1
        val maxSearchPages = 50 // Buscar en hasta 50 páginas

        // Cargar múltiples páginas para obtener un rango amplio de fotos
        var pageIndex = 0
        while (pageIndex < maxSearchPages) {
            val searchResult = loadItems(apiClient, searchPage + pageIndex, FETCH_PAGE_COUNT)

            when (searchResult) {
                is Either.Right -> {
                    val items = filterItems(searchResult.value)
                    if (items.isNotEmpty()) {
                        allFoundItems.addAll(items)

                        // Log del rango de fechas encontrado
                        val dates = items.mapNotNull { getCurrentAssetDate(it) }.sorted()
                        if (dates.isNotEmpty()) {
                            val firstDate = dates.first()
                            val lastDate = dates.last()
                            val firstCal = Calendar.getInstance().apply { time = firstDate }
                            val lastCal = Calendar.getInstance().apply { time = lastDate }
                            Timber.d("Página ${searchPage + pageIndex}: ${firstCal.get(Calendar.YEAR)}/${firstCal.get(Calendar.MONTH)+1} - ${lastCal.get(Calendar.YEAR)}/${lastCal.get(Calendar.MONTH)+1}")
                        }
                    } else {
                        // Si una página está vacía, probablemente hemos llegado al final
                        break
                    }
                }
                is Either.Left -> {
                    Timber.e("Error cargando página ${searchPage + pageIndex}: ${searchResult.value}")
                    break
                }
            }
            pageIndex++
        }

        Timber.d("Total de fotos cargadas para búsqueda: ${allFoundItems.size}")

        if (allFoundItems.isEmpty()) {
            return emptyList()
        }

        // Estrategia 2: Filtrar fotos por el período objetivo
        val matchingItems = allFoundItems.filter { item ->
            val itemDate = getCurrentAssetDate(item)
            if (itemDate != null) {
                val itemCalendar = Calendar.getInstance().apply { time = itemDate }
                when (navigationMode) {
                    NavigationMode.YEAR_BY_YEAR -> {
                        itemCalendar.get(Calendar.YEAR) == targetYear
                    }
                    NavigationMode.MONTH_BY_MONTH -> {
                        itemCalendar.get(Calendar.YEAR) == targetYear &&
                                itemCalendar.get(Calendar.MONTH) == targetMonth
                    }
                    else -> false
                }
            } else false
        }

        if (matchingItems.isNotEmpty()) {
            Timber.d("Encontradas ${matchingItems.size} fotos para el período objetivo")
            return matchingItems
        }

        // Estrategia 3: Si no encontramos el período exacto, buscar el más cercano
        Timber.d("No se encontraron fotos para el período exacto, buscando el más cercano...")

        // Obtener todas las fechas disponibles y ordenarlas
        val allDatesWithItems = allFoundItems.mapNotNull { item ->
            getCurrentAssetDate(item)?.let { date -> date to item }
        }.sortedBy { it.first }

        if (allDatesWithItems.isEmpty()) {
            return emptyList()
        }

        // Buscar la fecha más cercana al objetivo
        val targetTime = targetDate.time
        var closestItem: ITEM? = null
        var minDifference = Long.MAX_VALUE

        for ((itemDate, item) in allDatesWithItems) {
            val difference = Math.abs(targetTime - itemDate.time)
            if (difference < minDifference) {
                minDifference = difference
                closestItem = item
            }
        }

        // Si encontramos una foto cercana, buscar todas las fotos del mismo período
        closestItem?.let { item ->
            val closestDate = getCurrentAssetDate(item)
            if (closestDate != null) {
                val closestCalendar = Calendar.getInstance().apply { time = closestDate }
                val closestYear = closestCalendar.get(Calendar.YEAR)
                val closestMonth = closestCalendar.get(Calendar.MONTH)

                val closestPeriodItems = allFoundItems.filter { periodItem ->
                    val periodDate = getCurrentAssetDate(periodItem)
                    if (periodDate != null) {
                        val periodCalendar = Calendar.getInstance().apply { time = periodDate }
                        when (navigationMode) {
                            NavigationMode.YEAR_BY_YEAR -> {
                                periodCalendar.get(Calendar.YEAR) == closestYear
                            }
                            NavigationMode.MONTH_BY_MONTH -> {
                                periodCalendar.get(Calendar.YEAR) == closestYear &&
                                        periodCalendar.get(Calendar.MONTH) == closestMonth
                            }
                            else -> false
                        }
                    } else false
                }

                if (closestPeriodItems.isNotEmpty()) {
                    val foundPeriodName = when (navigationMode) {
                        NavigationMode.YEAR_BY_YEAR -> "año $closestYear"
                        NavigationMode.MONTH_BY_MONTH -> {
                            val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(closestDate)
                            "$monthName $closestYear"
                        }
                        else -> "período cercano"
                    }
                    Timber.d("Encontradas ${closestPeriodItems.size} fotos para el $foundPeriodName (el más cercano disponible)")
                    return closestPeriodItems
                }
            }
        }

        return emptyList()
    }

    // Nueva función para reiniciar la vista con fotos encontradas
    private fun resetViewWithNewPhotos(newPhotos: List<ITEM>, targetDate: Date, periodName: String) {
        // Limpiar estado actual
        clearState()

        // Establecer nuevos datos
        assets = sortItems(newPhotos)
        assetsStillToRender.clear()
        assetsStillToRender.addAll(assets)

        // Actualizar adapter
        adapter.clear()
        addAssetsPaginated()

        // Encontrar la mejor foto para posicionar el cursor
        val bestIndex = findBestAssetByDate(targetDate)
        if (bestIndex != -1) {
            updateManualPositionHandler(bestIndex)
            val foundAsset = assets[bestIndex]
            val foundDate = getCurrentAssetDate(foundAsset)

            if (foundDate != null) {
                Toast.makeText(
                    requireContext(),
                    "Fotos cargadas para el $periodName: ${formatPhotoDate(foundDate)}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Fotos cargadas para el $periodName",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Ocultar progress bar y cargar background
        progressBar?.visibility = View.GONE
        assets.firstOrNull()?.let {
            loadBackground(getBackgroundPicture(it)) {}
        }
    }

    // Nueva función auxiliar para búsqueda de fechas (día/semana)
    private fun loadMoreDataAndSearchForDate(targetDate: Date, direction: Int) {
        ioScope.launch {
            var found = false
            var attempts = 0
            val maxAttempts = 10

            while (!found && !allPagesLoaded && attempts < maxAttempts) {
                attempts++

                val moreAssets = loadMoreAssets()

                if (moreAssets.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        val targetIndex = findBestAssetByDate(targetDate)

                        if (targetIndex != -1 && targetIndex != currentSelectedIndex) {
                            found = true
                            val foundAsset = assets[targetIndex]
                            val foundDate = getCurrentAssetDate(foundAsset)

                            if (foundDate != null) {
                                updateManualPositionHandler(targetIndex)

                                val periodName = when (currentNavigationMode) {
                                    NavigationMode.WEEK_BY_WEEK -> if (direction > 0) "semana siguiente" else "semana anterior"
                                    NavigationMode.DAY_BY_DAY -> if (direction > 0) "día siguiente" else "día anterior"
                                    else -> "período"
                                }

                                Toast.makeText(
                                    requireContext(),
                                    "Navegando al $periodName: ${formatPhotoDate(foundDate)}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    break
                }
            }

            if (!found) {
                withContext(Dispatchers.Main) {
                    val periodName = when (currentNavigationMode) {
                        NavigationMode.WEEK_BY_WEEK -> if (direction > 0) "semana siguiente" else "semana anterior"
                        NavigationMode.DAY_BY_DAY -> if (direction > 0) "día siguiente" else "día anterior"
                        else -> "período"
                    }

                    Toast.makeText(
                        requireContext(),
                        "No hay fotos disponibles para el $periodName",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
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

    // Nueva función para buscar específicamente por año
    private fun findAssetByYear(targetYear: Int): Int {
        for (i in assets.indices) {
            val assetDate = getCurrentAssetDate(assets[i]) ?: continue
            val assetCalendar = Calendar.getInstance().apply { time = assetDate }
            val assetYear = assetCalendar.get(Calendar.YEAR)

            if (assetYear == targetYear) {
                return i // Encontramos la primera foto de ese año
            }
        }
        return -1
    }

    // Nueva función para buscar específicamente por año y mes
    private fun findAssetByYearAndMonth(targetYear: Int, targetMonth: Int): Int {
        for (i in assets.indices) {
            val assetDate = getCurrentAssetDate(assets[i]) ?: continue
            val assetCalendar = Calendar.getInstance().apply { time = assetDate }
            val assetYear = assetCalendar.get(Calendar.YEAR)
            val assetMonth = assetCalendar.get(Calendar.MONTH)

            if (assetYear == targetYear && assetMonth == targetMonth) {
                return i // Encontramos la primera foto de ese año y mes
            }
        }
        return -1
    }

    // Nueva función que busca de manera más inteligente
    private fun findBestAssetByDate(targetDate: Date): Int {
        if (assets.isEmpty()) return -1

        var bestIndex = -1
        var bestDate: Date? = null
        var minDifference = Long.MAX_VALUE

        // Buscar la foto con la fecha más cercana a la objetivo
        for (i in assets.indices) {
            val assetDate = getCurrentAssetDate(assets[i]) ?: continue
            val difference = Math.abs(targetDate.time - assetDate.time)

            if (difference < minDifference) {
                minDifference = difference
                bestIndex = i
                bestDate = assetDate
            }
        }

        // Si encontramos una foto, verificar que sea razonablemente cercana
        if (bestDate != null) {
            val daysDifference = minDifference / (1000 * 60 * 60 * 24)

            // Si la diferencia es muy grande (más de 6 meses), buscar en los límites
            if (daysDifference > 180) {
                val firstAssetDate = getCurrentAssetDate(assets.first())
                val lastAssetDate = getCurrentAssetDate(assets.last())

                if (firstAssetDate != null && lastAssetDate != null) {
                    val diffToFirst = Math.abs(targetDate.time - firstAssetDate.time)
                    val diffToLast = Math.abs(targetDate.time - lastAssetDate.time)

                    return if (diffToFirst < diffToLast) 0 else assets.size - 1
                }
            }
        }

        return bestIndex
    }

    protected fun updateNavigationModeDisplay() {
        // En lugar de mostrar el modo de navegación, mostrar información de fecha relevante
        updateDateRangeDisplay()
        updateDateContext()
    }

    private fun updateDateRangeDisplay() {
        // Esta función ya no es necesaria porque eliminamos los elementos de la UI
        // Antes mostraba información de fecha en la esquina superior izquierda
        // Ahora esa información se muestra solo en el lado derecho
    }

    private fun updateDateContext() {
        val currentAsset = getCurrentSelectedAsset()
        when {
            currentAsset == null -> return
            currentNavigationMode == NavigationMode.PHOTO_BY_PHOTO -> {
                formatPhotoDate(getCurrentAssetDate(currentAsset))
            }
            else -> formatTimeContextDate(getCurrentAssetDate(currentAsset))
        }
        // Elemento eliminado - la fecha ya no se muestra en la esquina superior izquierda
        // currentDateContext?.text = dateText
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

        // Guardar en preferencias y actualizar el modo actual
        PreferenceManager.save(NAVIGATION_MODE, nextMode)
        currentNavigationMode = nextMode
        updateNavigationModeDisplay()

        // Mostrar un mensaje para indicar el cambio
        Toast.makeText(
            requireContext(),
            "Modo de navegación: ${nextMode.getTitle()}",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateNavigationModeDisplay()
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

    // Función pública para navegación por año o mes desde los settings
    fun navigateByYearOrMonth(period: NavigationMode, direction: Int) {
        val currentAsset = getCurrentSelectedAsset() ?: return
        val currentDate = getCurrentAssetDate(currentAsset) ?: return
        val calendar = Calendar.getInstance().apply { time = currentDate }

        // Aplicar el cambio de tiempo según el período
        when (period) {
            NavigationMode.MONTH_BY_MONTH -> {
                calendar.add(Calendar.MONTH, direction)
                navigateToDateWithFreshLoad(calendar.time, period, direction)
            }
            NavigationMode.YEAR_BY_YEAR -> {
                calendar.add(Calendar.YEAR, direction)
                navigateToDateWithFreshLoad(calendar.time, period, direction)
            }
            else -> {
                // Para otros modos, usar la navegación normal
                currentNavigationMode = period
                navigateByTimeMode(direction)
            }
        }
    }

    companion object {
        const val COLUMNS = 4
        private const val FETCH_NEXT_THRESHOLD = COLUMNS * 6
        const val FETCH_COUNT = 50
        const val FETCH_PAGE_COUNT = FETCH_COUNT
    }
}

