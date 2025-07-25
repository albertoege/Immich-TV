package com.albertoeg.android.tv.immich.people

import androidx.navigation.fragment.findNavController
import arrow.core.Either
import com.albertoeg.android.tv.immich.VerticalCardGridFragment
import com.albertoeg.android.tv.immich.api.ApiClient
import com.albertoeg.android.tv.immich.api.model.Person
import com.albertoeg.android.tv.immich.api.util.ApiUtil
import com.albertoeg.android.tv.immich.card.Card
import com.albertoeg.android.tv.immich.home.HomeFragmentDirections

class PeopleFragment : VerticalCardGridFragment<Person>() {

    override fun sortItems(items: List<Person>): List<Person> {
       return items
    }

    override suspend fun loadItems(
        apiClient: ApiClient,
        page: Int,
        pageCount: Int
    ): Either<String, List<Person>> {
        if (page == startPage) {
            // no pagination possible yet!
            return apiClient.listPeople()
        }
        return Either.Right(emptyList())
    }

    override fun onItemSelected(card: Card, indexOf: Int) {
       // no use case yet
    }

    override fun onItemClicked(card: Card) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToPersonAssetsFragment(
                card.id,
                card.title
            )
        )
    }

    override fun getBackgroundPicture(it: Person): String? {
        return null
    }

    override fun createCard(a: Person): Card {
        return Card(
            a.name ?: "Unknown",
            "",
            a.id.toString(),
            ApiUtil.getPersonThumbnail(a.id),
            ApiUtil.getPersonThumbnail(a.id),
            false
        )
    }
}