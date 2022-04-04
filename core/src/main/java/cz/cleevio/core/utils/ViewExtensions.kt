package cz.cleevio.core.utils

import android.view.View

fun View.showOrGone(boolean: Boolean?) {
	this.showOrGone(boolean ?: false)
}

fun View.showOrGone(boolean: Boolean) {
	this.visibility = if (boolean) View.VISIBLE else View.GONE
}

fun View.showOrInvisible(boolean: Boolean?) {
	this.showOrInvisible(boolean ?: false)
}

fun View.showOrInvisible(boolean: Boolean) {
	this.visibility = if (boolean) View.VISIBLE else View.INVISIBLE
}
