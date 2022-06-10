package cz.cleevio.core.utils

import java.math.BigDecimal
import java.text.DecimalFormat

fun BigDecimal.formatAsPercentage(): String =
	DecimalFormat("#,###.##").format(this)
