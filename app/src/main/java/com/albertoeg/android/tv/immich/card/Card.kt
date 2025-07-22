package com.albertoeg.android.tv.immich.card

import java.util.Date

data class Card(
    override val title: String,
    override val description: String?,
    override val id: String,
    override val thumbnailUrl: String?,
    override val backgroundUrl: String?,
    override var selected: Boolean = false,
    val date: Date? = null // Agregar campo de fecha para mostrar en la tarjeta
) : ICard
