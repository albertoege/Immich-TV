package com.albertoeg.android.tv.immich.shared.util

import nl.giejay.mediaslider.model.SliderItem
import nl.giejay.mediaslider.model.SliderItemType
import nl.giejay.mediaslider.model.SliderItemViewHolder
import com.albertoeg.android.tv.immich.api.util.ApiUtil
import com.albertoeg.android.tv.immich.api.model.Asset
import com.albertoeg.android.tv.immich.card.Card
import nl.giejay.mediaslider.model.MetaDataType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun List<Asset>.toSliderItems(keepOrder: Boolean, mergePortrait: Boolean): List<SliderItemViewHolder> {
    if(!mergePortrait){
        return this.map{ SliderItemViewHolder(it.toSliderItem()) }
    }
    if (!keepOrder) {
        val portraitItems = this.filter { it.isPortraitImage() }.sortedWith(compareBy<Asset> { it.people?.firstOrNull()?.id }.thenBy { it.people?.size }.thenBy { it.exifInfo?.city})
        val landscapeItems = this.minus(portraitItems.toSet())

        val portraitSliders = portraitItems.chunked(2).map { SliderItemViewHolder(it.first().toSliderItem(), it.getOrNull(1)?.toSliderItem()) }
        return (landscapeItems.map { SliderItemViewHolder(it.toSliderItem()) } + portraitSliders).shuffled()
    }
    val queue = this.toMutableList()
    val items = mutableListOf<SliderItemViewHolder>()

    while (queue.isNotEmpty()) {
        val first = queue.removeAt(0)
        val second = queue.firstOrNull()

        if (first.isPortraitImage() && second?.isPortraitImage() == true) {
            items.add(SliderItemViewHolder(first.toSliderItem(), second.toSliderItem()))
            queue.remove(second)
        } else {
            items.add(SliderItemViewHolder(first.toSliderItem()))
        }
    }
    return items
}

fun Asset.toSliderItem(): SliderItem {
    return SliderItem(
        this.id,
        ApiUtil.getFileUrl(this.id, this.type),
        SliderItemType.valueOf(this.type.uppercase()),
        mapOf(
            MetaDataType.DATE to this.exifInfo?.dateTimeOriginal?.let{formatDate(it)},
            MetaDataType.CITY to this.exifInfo?.city,
            MetaDataType.COUNTRY to this.exifInfo?.country,
            MetaDataType.ALBUM_NAME to this.albumName,
            MetaDataType.DESCRIPTION to this.exifInfo?.description,
            MetaDataType.FILENAME to this.originalFileName,
            MetaDataType.FILEPATH to this.originalPath,
            MetaDataType.CAMERA to (listOf(this.exifInfo?.make, this.exifInfo?.model)).filterNotNull().joinToString(" ")
        ),
        ApiUtil.getThumbnailUrl(this.id, "preview")
    )
}

private fun formatDate(date: Date): String {
    val calendar = Calendar.getInstance()
    calendar.time = date
    val locale = Locale.getDefault(Locale.Category.FORMAT)
    val isEnglish = locale.language == "en"
    val day = calendar[Calendar.DATE]
    val formatString = if (isEnglish) {
        // English: Friday, 7th April 2006
        when (day) {
            1, 21, 31 -> "EEEE, d'st' MMMM yyyy"
            2, 22 -> "EEEE, d'nd' MMMM yyyy"
            3, 23 -> "EEEE, d'rd' MMMM yyyy"
            else -> "EEEE, d'th' MMMM yyyy"
        }
    } else {
        // All other locales: Freitag, 7. April 2006
        "EEEE, d. MMMM yyyy"
    }
    return SimpleDateFormat(formatString, locale).format(date)
}

fun Asset.isPortraitImage(): Boolean {
    return (this.exifInfo?.orientation == 6 || (this.exifInfo?.exifImageWidth != null && this.exifInfo.exifImageHeight != null && this.exifInfo.exifImageWidth - 100 < this.exifInfo.exifImageHeight)) && this.type == SliderItemType.IMAGE.toString()
}

fun List<Asset>.toCards(): List<Card> {
    return this.map {
        it.toCard()
    }
}

fun Asset.toCard(): Card {
    return Card(
        this.deviceAssetId ?: "",
        this.exifInfo?.description ?: "",
        this.id,
        ApiUtil.getThumbnailUrl(this.id, "thumbnail"),
        ApiUtil.getThumbnailUrl(this.id, "preview"),
        false, // selected
        this.exifInfo?.dateTimeOriginal ?: this.fileModifiedAt // Agregar fecha del asset
    )
}
