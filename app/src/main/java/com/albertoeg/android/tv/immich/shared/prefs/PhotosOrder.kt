package com.albertoeg.android.tv.immich.shared.prefs

import com.albertoeg.android.tv.immich.api.model.Asset
import com.albertoeg.android.tv.immich.shared.util.Utils.compareToNullSafe

enum class PhotosOrder(val sort: Comparator<Asset>): EnumWithTitle {
    NEWEST_OLDEST(
        { a1, a2 ->
            (a2.exifInfo?.dateTimeOriginal ?: a2.fileModifiedAt)?.compareToNullSafe(
                a1.exifInfo?.dateTimeOriginal ?: a1.fileModifiedAt
            ) ?: 1
        }
    ) {
        override fun getTitle(): String {
            return "Newest - Oldest"
        }
    },
    OLDEST_NEWEST(
        { a1, a2 ->
            (a1.exifInfo?.dateTimeOriginal ?: a1.fileModifiedAt)?.compareToNullSafe(
                a2.exifInfo?.dateTimeOriginal ?: a2.fileModifiedAt
            ) ?: 1
        }
    ) {
        override fun getTitle(): String {
            return "Oldest - Newest"
        }
    },
    ALPHABETICALLY_A_Z({ a1, a2 -> a1.deviceAssetId?.compareToNullSafe(a2.deviceAssetId) ?: 1 }) {
        override fun getTitle(): String {
            return "Alphabetically (A-Z)"
        }
    },
    ALPHABETICALLY_Z_A({ a2, a1 -> a1.deviceAssetId?.compareToNullSafe(a2.deviceAssetId) ?: 1 }) {
        override fun getTitle(): String {
            return "Alphabetically (Z-A)"
        }
    };

    companion object {
        fun valueOfSafe(name: String, default: PhotosOrder): PhotosOrder{
            return entries.find { it.toString() == name } ?: default
        }
    }
}