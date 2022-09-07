package cz.cleevio.core.utils

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import java.util.*

fun Int.formatCurrency(currency: String, locale: Locale) =
	CurrencyUtils.currencyInLocale(currency, locale).format(this)


fun Int.toDp(context: Context) = (this * context.resources.displayMetrics.density).toInt()

fun Int.x() = TypedValue.applyDimension(
	TypedValue.COMPLEX_UNIT_DIP,
	this.toFloat(),
	Resources.getSystem().displayMetrics
).toInt()