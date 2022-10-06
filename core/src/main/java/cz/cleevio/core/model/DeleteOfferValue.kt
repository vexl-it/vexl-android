package cz.cleevio.core.model

import cz.cleevio.core.widget.DeleteTimeframe
import java.util.concurrent.TimeUnit

const val DAY = 1L
const val WEEK = 7L
const val MONTH = 30L
const val TO_SECONDS = 1000L

data class DeleteOfferValue(
	val type: DeleteTimeframe,
	val value: Int
)

fun DeleteOfferValue.toUnixTimestamp(): Long {
	val modifier = when (this.type) {
		DeleteTimeframe.NONE -> 0
		DeleteTimeframe.DAYS -> TimeUnit.DAYS.toMillis(DAY)
		DeleteTimeframe.WEEKS -> TimeUnit.DAYS.toMillis(WEEK)
		DeleteTimeframe.MONTHS -> TimeUnit.DAYS.toMillis(MONTH)
	}
	return System.currentTimeMillis() / TO_SECONDS + modifier * value
}