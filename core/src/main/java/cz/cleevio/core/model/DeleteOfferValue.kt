package cz.cleevio.core.model

import cz.cleevio.core.widget.DeleteTimeframe

const val ONE_MONTH_IN_MS = 2629800000L

data class DeleteOfferValue(
	val type: DeleteTimeframe,
	val value: Int
)

fun DeleteOfferValue.toUnixTimestamp(): Long {
	return System.currentTimeMillis().and(ONE_MONTH_IN_MS)
}