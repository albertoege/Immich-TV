package com.albertoeg.android.tv.immich.card

import android.app.Activity
import android.content.Context
import android.view.ContextThemeWrapper
import android.widget.ImageView
import androidx.leanback.widget.ImageCardView
import com.bumptech.glide.Glide
import com.albertoeg.tv.immich.R
import com.albertoeg.android.tv.immich.shared.presenter.AbstractPresenter
import timber.log.Timber


open class CardPresenter(context: Context, style: Int = R.style.DefaultCardTheme) :
    AbstractPresenter<ImageCardView, ICard>(ContextThemeWrapper(context, style)) {

    override fun onBindViewHolder(card: ICard, cardView: ImageCardView) {
        loadImage(card, cardView)

        cardView.tag = card
        cardView.titleText = card.title
        if (card.description != "") {
            cardView.contentText = card.description
        }
        setSelected(cardView, card.selected)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        super.onUnbindViewHolder(viewHolder)
        if(context is Activity && context.isFinishing){
            return
        }
        try {
            Glide.with(context).clear((viewHolder.view as ImageCardView).mainImageView!!)
        } catch (e: IllegalArgumentException){
            Timber.e(e)
        }
    }

    open fun loadImage(card: ICard, cardView: ImageCardView) {
        card.thumbnailUrl?.let {
            if(it.startsWith("http")){
                Glide.with(context)
                    .asBitmap()
                    .centerInside()
                    .load(it)
                    .into(cardView.mainImageView!!)
            } else {
                cardView.mainImageView!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                val resourceId = context.resources.getIdentifier(it, "drawable",
                    context.packageName);
                cardView.mainImageView!!.setImageResource(resourceId)
            }
//                .addListener(object : RequestListener<Bitmap> {
//                    override fun onLoadFailed(
//                        e: GlideException?,
//                        model: Any?,
//                        target: Target<Bitmap>?,
//                        isFirstResource: Boolean
//                    ): Boolean {
//                        return false
//                    }
//
//                    override fun onResourceReady(
//                        resource: Bitmap?,
//                        model: Any?,
//                        target: Target<Bitmap>?,
//                        dataSource: DataSource?,
//                        isFirstResource: Boolean
//                    ): Boolean {
//                        Timber.i("Loaded card image for: ${card.id}")
//                        return false
//                    }
//
//                })
        }
    }

    override fun onCreateView(): ImageCardView {
        return ImageCardView(context)
    }

    private fun setSelected(imageCardView: ImageCardView, selected: Boolean) {
        if(selected){
            imageCardView.mainImageView!!.background = context.getDrawable(R.drawable.border)
        } else {
            imageCardView.mainImageView!!.background = null
        }
    }
}