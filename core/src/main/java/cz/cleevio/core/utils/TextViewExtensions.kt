package cz.cleevio.core.utils

import android.widget.TextView
import androidx.core.content.ContextCompat

fun TextView.setIcons(
	starIcon: Int?,
	endIcon: Int?,
	topIcon: Int?,
	bottomIcon: Int?
) {
	setCompoundDrawablesWithIntrinsicBounds(
		starIcon?.let { ContextCompat.getDrawable(context, it) },
		topIcon?.let { ContextCompat.getDrawable(context, it) },
		endIcon?.let { ContextCompat.getDrawable(context, it) },
		bottomIcon?.let { ContextCompat.getDrawable(context, it) }
	)
}