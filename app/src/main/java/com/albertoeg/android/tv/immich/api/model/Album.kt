package com.albertoeg.android.tv.immich.api.model

import java.util.Date

data class Album(
    val albumName: String,
    val description: String,
    val id: String,
    val albumThumbnailAssetId: String?,
    val updatedAt: Date,
    val endDate: Date?,
    val assetCount: Int
)