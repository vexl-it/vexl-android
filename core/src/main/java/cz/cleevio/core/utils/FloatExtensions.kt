package cz.cleevio.core.utils

import java.util.*

fun Float.formatCurrency(currency: String, locale: Locale) =
	CurrencyUtils.currencyInLocale(currency, locale).format(this)