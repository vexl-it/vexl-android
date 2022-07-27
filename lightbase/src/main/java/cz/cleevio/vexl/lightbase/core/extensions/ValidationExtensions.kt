package cz.cleevio.vexl.lightbase.core.extensions

import android.util.Patterns

fun String?.isNotNullOrBlank(): Boolean = !this.isNullOrBlank()

fun String?.isValidAsEmail(): Boolean =
	!this.isNullOrBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
