package cz.cleevio.core.model

import cz.cleevio.core.widget.DeleteTimeframe

const val ONE_MONTH_IN_MS = 2_629_800_000L

data class DeleteOfferValue(
	val type: DeleteTimeframe,
	val value: Int
)

//right now hardcoded 1 MONTH fixme: change to parse DeleteOfferValue
fun DeleteOfferValue.toUnixTimestamp(): Long = System.currentTimeMillis().and(ONE_MONTH_IN_MS)