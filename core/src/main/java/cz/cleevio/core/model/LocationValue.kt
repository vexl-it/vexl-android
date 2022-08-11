package cz.cleevio.core.model

import cz.cleevio.core.widget.LocationButtonSelected
import cz.cleevio.repository.model.offer.Location

data class LocationValue constructor(
	val type: LocationButtonSelected,
	val values: List<Location>
) {
	fun isTypeNone() = type == LocationButtonSelected.NONE
}
