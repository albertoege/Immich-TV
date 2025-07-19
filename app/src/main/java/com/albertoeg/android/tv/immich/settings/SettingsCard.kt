package com.albertoeg.android.tv.immich.settings

import com.albertoeg.android.tv.immich.card.ICard

class SettingsCard(
    override val title: String,
    override val description: String?,
    override val id: String,
    override val thumbnailUrl: String?,
    override val backgroundUrl: String?,
    override val selected: Boolean = false,
    val visible: Boolean = true,
    val onClick: () -> Unit,
) : ICard {
}