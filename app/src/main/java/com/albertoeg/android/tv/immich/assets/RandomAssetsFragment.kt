package com.albertoeg.android.tv.immich.assets

import arrow.core.Either
import com.albertoeg.android.tv.immich.api.ApiClient
import com.albertoeg.android.tv.immich.api.model.Asset

class RandomAssetsFragment : GenericAssetFragment() {

    override suspend fun loadItems(
        apiClient: ApiClient,
        page: Int,
        pageCount: Int
    ): Either<String, List<Asset>> {
        return apiClient.listAssets(page,
            pageCount,
            random = true,
            contentType = currentFilter)
    }
}