package cz.cleevio.core.utils

import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*

fun BigDecimal.formatAsPercentage(): String =
	DecimalFormat("#,###.##").format(this)

fun BigDecimal.formatCurrency(currency: String, locale: Locale) =
	CurrencyUtils.currencyInLocale(currency, locale).format(this)