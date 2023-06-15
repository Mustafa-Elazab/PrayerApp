package com.mostafa.alaymiatask.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.mostafa.alaymiatask.R


@BindingAdapter("loadImage")
fun ImageView.loadImage(imageUrl: String?) {
    imageUrl?.let {
        Glide.with(context)
            .load(it)
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(R.drawable.ic_no_internet)
            .into(this)
    }
}


@BindingAdapter("loadLocalImage")
fun setImageViewResource(imageView: ImageView, resource: Int) {
    imageView.setImageResource(resource)
}