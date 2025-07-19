package com.albertoeg.android.tv.immich.home

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.leanback.widget.RowHeaderPresenter
import com.albertoeg.tv.immich.R

class ImmichRowViewHolder(view: View) : RowHeaderPresenter.ViewHolder(view) {
    val tvTitle: TextView
    val icon: ImageView
    init {
        tvTitle = view.findViewById(R.id.tvTitle)
        icon = view.findViewById(R.id.row_visible)
    }
}