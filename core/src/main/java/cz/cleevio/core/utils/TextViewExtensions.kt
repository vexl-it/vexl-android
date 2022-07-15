package cz.cleevio.core.utils

import android.content.Context
import android.view.animation.Animation
import android.view.animation.AnimationUtils
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

fun animateTextChange(context: Context, textView: TextView, animationId: Int, newText: String): Animation {
	val animation = AnimationUtils.loadAnimation(context, animationId)
	animation.setAnimationListener(object : Animation.AnimationListener {
		override fun onAnimationStart(animation: Animation?) {
			textView.text = newText
		}
		override fun onAnimationEnd(animation: Animation?) {
			// Do nothing
		}
		override fun onAnimationRepeat(animation: Animation?) {
			// Do nothing
		}
	})

	return animation
}