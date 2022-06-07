package cz.cleevio.core.model

import cz.cleevio.core.widget.DeleteTimeframe

data class DeleteOfferValue(
	val type: DeleteTimeframe,
	val value: Int
)