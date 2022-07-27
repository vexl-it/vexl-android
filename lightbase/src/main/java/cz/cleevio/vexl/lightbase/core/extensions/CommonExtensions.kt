package cz.cleevio.vexl.lightbase.core.extensions

import android.content.Context
import androidx.core.content.ContextCompat

fun Context.color(res: Int): Int =
	ContextCompat.getColor(this, res)

fun Context.dpValueToPx(dpValue: Int): Float = (this.resources.displayMetrics.density * dpValue)

inline fun <A, B, R> ifNotNull(a: A?, b: B?, code: (A, B) -> R): R? {
	return if (a != null && b != null) {
		code(a, b)
	} else {
		null
	}
}
