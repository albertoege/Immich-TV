package com.albertoeg.android.tv.immich.people

import android.os.Bundle
import arrow.core.Either
import com.albertoeg.android.tv.immich.api.ApiClient
import com.albertoeg.android.tv.immich.api.model.Asset
import com.albertoeg.android.tv.immich.assets.GenericAssetFragment
import java.util.UUID

class PersonAssetsFragment : GenericAssetFragment() {
    private lateinit var personId: String
    private lateinit var personName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        personId = PersonAssetsFragmentArgs.fromBundle(requireArguments()).personId
        personName = PersonAssetsFragmentArgs.fromBundle(requireArguments()).personName
        super.onCreate(savedInstanceState)
    }

    override suspend fun loadItems(
        apiClient: ApiClient,
        page: Int,
        pageCount: Int
    ): Either<String, List<Asset>> {
        return apiClient.listAssets(page,
            pageCount,
            random = true,
            order = "desc",
            contentType = currentFilter,
            personIds = listOf(UUID.fromString(personId))).map { it.shuffled() }
    }

    override fun showMediaCount(): Boolean {
        return false
    }


    override fun setTitle(response: List<Asset>) {
        title = personName
    }
}