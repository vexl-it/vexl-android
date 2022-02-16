package cz.cleevio.core.utils

import java.text.Normalizer

fun String.stripAccents(): String {
	var normalize = Normalizer.normalize(this.toLowerCase(), Normalizer.Form.NFD)
	normalize = normalize.replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
	return normalize
}

fun String.toDoubleOrNullable(): Double? =
	this.replace(',', '.').toDoubleOrNull()