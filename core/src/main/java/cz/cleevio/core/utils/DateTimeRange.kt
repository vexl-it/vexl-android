package cz.cleevio.core.utils

import com.lyft.kronos.KronosClock
import cz.cleevio.network.request.market.MarketChartRequest
import lightbase.core.extensions.formatToUTC
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

const val ONE_HOUR_IN_SECONDS: Long = 60 * 60
const val ONE_DAY_IN_SECONDS: Long = ONE_HOUR_IN_SECONDS * 24
const val ONE_WEEK_IN_SECONDS: Long = ONE_DAY_IN_SECONDS * 7

enum class DateTimeRange {
	HOUR,
	DAY,
	WEEK,
	MONTH,
	THREE_MONTHS,
	SIX_MONTHS,
	YEAR;
	
	fun getRange(kronosClock: KronosClock): MarketChartRequest {
		val instantTo = Instant.ofEpochMilli(kronosClock.getCurrentTimeMs())
		val fromDateTime = when (this) {
			HOUR -> {
				instantTo.minusSeconds(ONE_HOUR_IN_SECONDS)
					.formatToUTC()
			}
			DAY -> {
				instantTo.minusSeconds(ONE_DAY_IN_SECONDS)
					.formatToUTC()
			}
			WEEK -> {
				instantTo.minusSeconds(ONE_WEEK_IN_SECONDS)
					.formatToUTC()
			}
			MONTH -> {
				ZonedDateTime.now(ZoneOffset.UTC)
					.minusMonths(1)
					.formatToUTC()
			}
			THREE_MONTHS -> {
				ZonedDateTime.now(ZoneOffset.UTC)
					.minusMonths(3)
					.formatToUTC()
			}
			SIX_MONTHS -> {
				ZonedDateTime.now(ZoneOffset.UTC)
					.minusMonths(6)
					.formatToUTC()
			}
			YEAR -> {
				ZonedDateTime.now(ZoneOffset.UTC)
					.minusYears(1)
					.formatToUTC()
			}
		}
		return MarketChartRequest(
			from = fromDateTime,
			to = instantTo.formatToUTC(),
			currency = "bitcoin"
		)
	}
}