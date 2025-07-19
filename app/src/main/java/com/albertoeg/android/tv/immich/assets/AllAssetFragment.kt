package com.albertoeg.android.tv.immich.assets

import arrow.core.Either
import com.albertoeg.android.tv.immich.api.ApiClient
import com.albertoeg.android.tv.immich.api.model.Asset
import com.albertoeg.android.tv.immich.shared.prefs.PhotosOrder
import com.albertoeg.android.tv.immich.shared.prefs.PreferenceManager
import com.albertoeg.android.tv.immich.shared.prefs.SLIDER_SHOW_MEDIA_COUNT

class AllAssetFragment : GenericAssetFragment() {

    override suspend fun loadItems(
        apiClient: ApiClient,
        page: Int,
        pageCount: Int
    ): Either<String, List<Asset>> {
        return apiClient.listAssets(page,
            pageCount,
            false,
            if (currentSort == PhotosOrder.NEWEST_OLDEST) "desc" else "asc",
            contentType = currentFilter)
    }

    override fun showMediaCount(): Boolean {
        return PreferenceManager.get(SLIDER_SHOW_MEDIA_COUNT)
    }
}