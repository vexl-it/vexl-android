package cz.cleevio.core.model

import cz.cleevio.core.widget.FeeButtonSelected

data class FeeValue constructor(
	val type: FeeButtonSelected,
	val value: Float
) {
	fun isWithFee() = type == FeeButtonSelected.WITH_FEE
}
