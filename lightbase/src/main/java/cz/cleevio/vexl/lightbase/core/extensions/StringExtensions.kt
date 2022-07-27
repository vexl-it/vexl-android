package cz.cleevio.vexl.lightbase.core.extensions

import android.text.TextUtils
import android.util.Patterns
import java.text.Normalizer
import java.util.*

fun String.isValidAsEmail(): Boolean =
	!TextUtils.isEmpty(this) && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.stripAccents(): String {
	var normalize = Normalizer.normalize(this.lowercase(Locale.getDefault()), Normalizer.Form.NFD)
	normalize = normalize.replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
	return normalize
}
