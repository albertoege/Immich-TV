package com.albertoeg.android.tv.immich.api.model

import java.util.Date

data class AssetExifInfo(
    val description: String?,
    val orientation: Int?,
    val exifImageWidth: Int?,
    val exifImageHeight: Int?,
    val city: String?,
    val country: String?,
    val dateTimeOriginal: Date?,
    val make: String?,
    val model: String?
)

data class Tag(
    val color: String?,
    val createdAt: Date,
    val name: String,
    val value: String
)

data class Asset(
    val id: String,
    val type: String,
    val deviceAssetId: String?,
    val exifInfo: AssetExifInfo?,
    val fileModifiedAt: Date?,
    val albumName: String?,
    val people: List<Person>?,
    val tags: List<Tag>?,
    val originalPath: String?,
    val originalFileName: String?
)
