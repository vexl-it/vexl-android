package cz.cleevio.core.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.Base64
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
		.replace("\\(".toRegex(), "")
		.replace("\\)".toRegex(), "")
		.replace("-".toRegex(), "")
}

fun String.isPhoneValid(): Boolean =
	this.matches("^[+][0-9]{10,13}$".toRegex())

fun String.getBitmap(): Bitmap {
	val decodedBase64 = Base64.decode(this, Base64.DEFAULT)
	return BitmapFactory.decodeByteArray(decodedBase64, 0, decodedBase64.size)
}

@Suppress("DEPRECATION")
fun fromHtml(text: String): Spanned {
	return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
		Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
	} else {
		Html.fromHtml(text)
	}
}