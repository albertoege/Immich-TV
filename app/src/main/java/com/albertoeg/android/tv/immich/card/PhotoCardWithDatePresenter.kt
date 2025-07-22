package com.albertoeg.android.tv.immich.card

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.leanback.widget.Presenter
import com.albertoeg.tv.immich.R
import com.bumptech.glide.Glide
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * CardPresenter personalizado para mostrar fotos con fecha en la esquina inferior izquierda
 */
class PhotoCardWithDatePresenter(private val context: Context) : Presenter() {

    class ViewHolder(view: View) : Presenter.ViewHolder(view) {
        val mainImage: ImageView = view.findViewById(R.id.main_image)
        val photoDate: TextView = view.findViewById(R.id.photo_date)
        // Eliminado titleText ya que no se muestra en el nuevo layout
    }

    override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.photo_card_with_date, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any?) {
        val holder = viewHolder as ViewHolder
        val card = item as ICard

        // Cargar la imagen
        loadImage(card, holder.mainImage)

        // Configurar la fecha
        setPhotoDate(card, holder.photoDate)

        // Configurar selección
        setSelected(holder.view, card.selected)
    }

    override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {
        val holder = viewHolder as ViewHolder
        if (context is Activity && context.isFinishing) {
            return
        }
        try {
            Glide.with(context).clear(holder.mainImage)
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
        }
    }

    private fun loadImage(card: ICard, imageView: ImageView) {
        card.thumbnailUrl?.let { url ->
            if (url.startsWith("http")) {
                Glide.with(context)
                    .asBitmap()
                    .centerCrop()
                    .load(url)
                    .into(imageView)
            } else {
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                val resourceId = context.resources.getIdentifier(
                    url, "drawable",
                    context.packageName
                )
                imageView.setImageResource(resourceId)
            }
        }
    }

    private fun setPhotoDate(card: ICard, dateTextView: TextView) {
        // Intentar obtener la fecha de la tarjeta
        val description = card.description // Crear variable local para el smart cast
        val dateString = when {
            !description.isNullOrEmpty() && isDateString(description) -> {
                // Si la descripción es una fecha, usarla
                formatDateString(description)
            }
            card is Card && card.date != null -> {
                // Si la tarjeta tiene una fecha específica
                formatDate(card.date)
            }
            else -> {
                // Si no hay fecha disponible, ocultar el TextView
                dateTextView.visibility = View.GONE
                return
            }
        }

        dateTextView.text = dateString
        dateTextView.visibility = View.VISIBLE
    }

    private fun isDateString(text: String?): Boolean {
        if (text.isNullOrEmpty()) return false
        // Verificar si el texto parece ser una fecha
        return text.matches(Regex("\\d{4}-\\d{2}-\\d{2}.*")) ||
               text.matches(Regex("\\d{2}/\\d{2}/\\d{4}.*")) ||
               (text.contains("T") && text.contains("Z")) // ISO format
    }

    private fun formatDateString(dateString: String): String {
        return try {
            // Intentar parsear diferentes formatos de fecha
            val formats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd",
                "dd/MM/yyyy"
            )

            for (format in formats) {
                try {
                    val sdf = SimpleDateFormat(format, Locale.getDefault())
                    val date = sdf.parse(dateString)
                    if (date != null) {
                        return formatDate(date)
                    }
                } catch (e: Exception) {
                    // Continuar con el siguiente formato
                }
            }

            // Si no se puede parsear, usar los primeros caracteres de la fecha
            if (dateString.length >= 10) {
                dateString.substring(0, 10)
            } else {
                dateString
            }
        } catch (e: Exception) {
            Timber.e(e, "Error formatting date string: $dateString")
            dateString.take(10) // Tomar los primeros 10 caracteres
        }
    }

    private fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return formatter.format(date)
    }

    private fun setSelected(view: View, selected: Boolean) {
        if (selected) {
            view.background = context.getDrawable(R.drawable.border)
        } else {
            view.background = null
        }
    }
}
