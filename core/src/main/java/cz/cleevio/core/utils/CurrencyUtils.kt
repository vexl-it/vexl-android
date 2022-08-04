package cz.cleevio.core.utils

import java.text.NumberFormat
import java.util.*

object CurrencyUtils {

	fun currencyInLocale(
		currencyCode: String,
		locale: Locale
	): NumberFormat =
		locale.let {
			NumberFormat.getCurrencyInstance(it).apply {
				currency = Currency.getInstance(currencyCode)
				maximumFractionDigits = 0
			}
		}
}