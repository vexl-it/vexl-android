package cz.cleevio.core.utils

private const val HOUR_IN_MILLIS = 3_600L
private const val DAY_IN_MILLIS = 86_400L

private const val DAYS_IN_WEEK = 7
private const val DAYS_IN_MONTH = 30
private const val DAYS_IN_THREE_MONTHS = 30 * 3
private const val DAYS_IN_SIX_MONTHS = 30 * 6
private const val DAYS_IN_YEAR = 365

enum class DateTimeRange {
	HOUR,
	DAY,
	WEEK,
	MONTH,
	THREE_MONTHS,
	SIX_MONTHS,
	YEAR;
}

fun getChartTimeRange(dateTimeRange: DateTimeRange): Pair<String, String> {
	val nowInMillis = System.currentTimeMillis()

	val timeInterval: Long = when (dateTimeRange) {
		DateTimeRange.HOUR -> HOUR_IN_MILLIS
		DateTimeRange.DAY -> DAY_IN_MILLIS
		DateTimeRange.WEEK -> DAY_IN_MILLIS * DAYS_IN_WEEK
		DateTimeRange.MONTH -> DAY_IN_MILLIS * DAYS_IN_MONTH
		DateTimeRange.THREE_MONTHS -> DAY_IN_MILLIS * DAYS_IN_THREE_MONTHS
		DateTimeRange.SIX_MONTHS -> DAY_IN_MILLIS * DAYS_IN_SIX_MONTHS
		DateTimeRange.YEAR -> DAY_IN_MILLIS * DAYS_IN_YEAR
	}

	// Reduce millis
	val fromDate = (nowInMillis - timeInterval) / 1000
	val toDate = nowInMillis / 1000

	return Pair(fromDate.toString(), toDate.toString())
}