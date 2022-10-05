package cz.cleevio.core.utils

import androidx.annotation.DrawableRes
import coil.request.ImageRequest

fun ImageRequest.Builder.setPlaceholders(@DrawableRes drawableResId: Int) {
	crossfade(true)
	fallback(drawableResId)
	error(drawableResId)
	placeholder(drawableResId)
}