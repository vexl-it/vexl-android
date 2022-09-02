package cz.cleevio.core.utils

import android.os.Build
import android.text.Html
import android.text.Spanned
import java.text.Normalizer
import java.util.*

fun String.stripAccents(): String {
	var normalize = Normalizer.normalize(this.lowercase(Locale.getDefault()), Normalizer.Form.NFD)
	normalize = normalize.replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
	return normalize
}

fun String.toDoubleOrNullable(): Double? =
	this.replace(',', '.').toDoubleOrNull()

fun String.toValidPhoneNumber(): String {
	return this
		.replace("\\s".toRegex(), "")
		.replace("-".toRegex(), "")
}

fun String.isPhoneValid(): Boolean =
	this.matches("^[+][0-9]{10,13}$".toRegex())

@Suppress("DEPRECATION")
fun fromHtml(text: String): Spanned {
	return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
		Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
	} else {
		Html.fromHtml(text)
	}
}