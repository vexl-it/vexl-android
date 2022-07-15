package cz.cleevio.core.utils

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

fun BigDecimal.formatAsPercentage(): String =
	DecimalFormat("###,###.#", DecimalFormatSymbols(Locale.US)).format(this)

fun BigDecimal.formatAsPrice(): String =
	DecimalFormat("###,###.#", DecimalFormatSymbols(Locale.US)).format(this).replace(",", " ")

fun BigDecimal.formatCurrency(currency: String, locale: Locale) =
	CurrencyUtils.currencyInLocale(currency, locale).format(this)