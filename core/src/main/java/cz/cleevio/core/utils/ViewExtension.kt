package cz.cleevio.core.utils

import android.os.SystemClock
import android.view.View
import kotlin.math.abs

fun View.setDebouncedOnClickListener(delay: Long = 1000L, onClick: (View) -> Unit) {
	this.setOnClickListener(object : View.OnClickListener {
		var previousClickTimestamp = 0L

		override fun onClick(view: View) {
			val currentTimestamp = SystemClock.elapsedRealtime()
			if (abs(currentTimestamp - previousClickTimestamp) > delay) {
				previousClickTimestamp = currentTimestamp
				onClick.invoke(view)
			}
		}
	})
}