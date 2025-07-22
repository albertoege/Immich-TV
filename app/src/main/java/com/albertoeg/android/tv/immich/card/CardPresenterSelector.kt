package com.albertoeg.android.tv.immich.card

import android.content.Context
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.PresenterSelector

class CardPresenterSelector(val context: Context): PresenterSelector() {
    private val photoCardPresenter = PhotoCardWithDatePresenter(context)
    private val defaultCardPresenter = CardPresenter(context)

    override fun getPresenter(item: Any?): Presenter {
        return when (item) {
            is Card -> {
                // Para las tarjetas de fotos (que tienen fecha), usar el presenter personalizado
                if (item.date != null || isPhotoCard(item)) {
                    photoCardPresenter
                } else {
                    defaultCardPresenter
                }
            }
            else -> defaultCardPresenter
        }
    }

    private fun isPhotoCard(card: Card): Boolean {
        // Determinar si es una tarjeta de foto basándose en características
        return card.thumbnailUrl?.contains("photo") == true ||
               card.thumbnailUrl?.contains("image") == true ||
               card.thumbnailUrl?.contains("asset") == true ||
               card.title.matches(Regex(".*\\.(jpg|jpeg|png|gif|bmp|webp)$", RegexOption.IGNORE_CASE))
    }
}