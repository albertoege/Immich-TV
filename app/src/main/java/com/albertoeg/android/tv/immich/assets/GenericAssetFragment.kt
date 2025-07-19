package com.albertoeg.android.tv.immich.assets

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.albertoeg.android.tv.immich.VerticalCardGridFragment
import com.albertoeg.android.tv.immich.album.AlbumDetailsFragmentDirections
import com.albertoeg.android.tv.immich.api.model.Asset
import com.albertoeg.android.tv.immich.api.util.ApiUtil
import com.albertoeg.android.tv.immich.card.Card
import com.albertoeg.android.tv.immich.home.HomeFragmentDirections
import com.albertoeg.android.tv.immich.shared.prefs.ALL_ASSETS_SORTING
import com.albertoeg.android.tv.immich.shared.prefs.ContentType
import com.albertoeg.android.tv.immich.shared.prefs.DEBUG_MODE
import com.albertoeg.android.tv.immich.shared.prefs.EnumByTitlePref
import com.albertoeg.android.tv.immich.shared.prefs.FILTER_CONTENT_TYPE
import com.albertoeg.android.tv.immich.shared.prefs.MetaDataScreen
import com.albertoeg.android.tv.immich.shared.prefs.NavigationMode
import com.albertoeg.android.tv.immich.shared.prefs.PhotosOrder
import com.albertoeg.android.tv.immich.shared.prefs.PreferenceManager
import com.albertoeg.android.tv.immich.shared.prefs.SCREENSAVER_ANIMATE_ASSET_SLIDE
import com.albertoeg.android.tv.immich.shared.prefs.SLIDER_ANIMATION_SPEED
import com.albertoeg.android.tv.immich.shared.prefs.SLIDER_GLIDE_TRANSFORMATION
import com.albertoeg.android.tv.immich.shared.prefs.SLIDER_INTERVAL
import com.albertoeg.android.tv.immich.shared.prefs.SLIDER_MAX_CUT_OFF_HEIGHT
import com.albertoeg.android.tv.immich.shared.prefs.SLIDER_MAX_CUT_OFF_WIDTH
import com.albertoeg.android.tv.immich.shared.prefs.SLIDER_MERGE_PORTRAIT_PHOTOS
import com.albertoeg.android.tv.immich.shared.prefs.SLIDER_ONLY_USE_THUMBNAILS
import com.albertoeg.android.tv.immich.shared.util.toCard
import com.albertoeg.android.tv.immich.shared.util.toSliderItems
import com.albertoeg.android.tv.immich.shared.prefs.NAVIGATION_MODE
import nl.giejay.mediaslider.util.LoadMore
import nl.giejay.mediaslider.config.MediaSliderConfiguration

abstract class GenericAssetFragment : VerticalCardGridFragment<Asset>() {
    protected lateinit var currentFilter: ContentType
    protected lateinit var currentSort: PhotosOrder

    override fun onCreate(savedInstanceState: Bundle?) {
        val sortingKey = getSortingKey()
        val filterKey = getFilterKey()
        currentSort = PreferenceManager.get(sortingKey)
        currentFilter = PreferenceManager.get(filterKey)
        currentNavigationMode = PreferenceManager.get(NAVIGATION_MODE)

        super.onCreate(savedInstanceState)

        PreferenceManager.subscribeMultiple(listOf(sortingKey, filterKey, NAVIGATION_MODE)) { state ->
            if(state[sortingKey.key()] != currentSort || state[filterKey.key()] != currentFilter || state[NAVIGATION_MODE.key()] != currentNavigationMode){
                clearState()
                currentSort = state[sortingKey.key()] as PhotosOrder
                currentFilter = state[filterKey.key()] as ContentType
                currentNavigationMode = state[NAVIGATION_MODE.key()] as NavigationMode
                updateNavigationModeDisplay()
                fetchInitialItems()
            }
        }
    }

    override fun filterItems(items: List<Asset>): List<Asset> {
        return items.filter { currentFilter ==  ContentType.ALL || it.type.lowercase() == currentFilter.toString().lowercase() }
    }

    open fun getSortingKey(): EnumByTitlePref<PhotosOrder>{
        return ALL_ASSETS_SORTING
    }

    open fun getFilterKey(): EnumByTitlePref<ContentType>{
        return FILTER_CONTENT_TYPE
    }

    override fun sortItems(items: List<Asset>): List<Asset> {
        return items.sortedWith(currentSort.sort)
    }

    override fun onItemSelected(card: Card, indexOf: Int) {
        // no use case yet
    }

    open fun showMediaCount(): Boolean {
        return false
    }

    override fun openPopUpMenu() {
        findNavController().navigate(
            HomeFragmentDirections.actionGlobalToSettingsDialog("generic_asset_settings")
        )
    }

    override fun onItemClicked(card: Card) {
        val toSliderItems = assets.toSliderItems(keepOrder = true, mergePortrait = PreferenceManager.get(SLIDER_MERGE_PORTRAIT_PHOTOS))
        val loadMore: LoadMore = suspend {
            val moreAssets = loadMoreAssets()
            // also load the data in the overview
            setDataOnMain(moreAssets)
            moreAssets.toSliderItems(true, PreferenceManager.get(SLIDER_MERGE_PORTRAIT_PHOTOS))
        }

        findNavController().navigate(
            AlbumDetailsFragmentDirections.actionToPhotoSlider(
                MediaSliderConfiguration(
                    toSliderItems.indexOfFirst { it.ids().contains(card.id) },
                    PreferenceManager.get(SLIDER_INTERVAL),
                    PreferenceManager.get(SLIDER_ONLY_USE_THUMBNAILS),
                    isVideoSoundEnable = true,
                    toSliderItems,
                    loadMore,
                    { item -> manualUpdatePosition(this.assets.indexOfFirst { item.ids().contains(it.id) }) },
                    animationSpeedMillis = PreferenceManager.get(SLIDER_ANIMATION_SPEED),
                    maxCutOffHeight = PreferenceManager.get(SLIDER_MAX_CUT_OFF_HEIGHT),
                    maxCutOffWidth = PreferenceManager.get(SLIDER_MAX_CUT_OFF_WIDTH),
                    transformation = PreferenceManager.get(SLIDER_GLIDE_TRANSFORMATION),
                    debugEnabled = PreferenceManager.get(DEBUG_MODE),
                    enableSlideAnimation = PreferenceManager.get(SCREENSAVER_ANIMATE_ASSET_SLIDE),
                    gradiantOverlay = false,
                    metaDataConfig = PreferenceManager.getAllMetaData(MetaDataScreen.VIEWER)
                )
            )
        )
    }

    override fun getBackgroundPicture(it: Asset): String? {
        return ApiUtil.getFileUrl(it.id, "IMAGE")
    }

    override fun createCard(a: Asset): Card {
        return a.toCard()
    }
}