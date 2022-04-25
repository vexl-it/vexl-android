package cz.cleevio.core.model

import cz.cleevio.core.widget.FeeButtonSelected

data class FeeValue(
	val type: FeeButtonSelected,
	val value: Int
)
