package cz.cleevio.repository.model.offer

import java.math.BigDecimal

data class LocationSuggestion constructor(
	val suggestFirstRow: String,
	val suggestSecondRow: String,
	val city: String,
	val region: String,
	val country: String,
	val latitude: BigDecimal,
	val longitude: BigDecimal
) {
	override fun toString() = cityText

	val cityText = city.ifBlank { suggestFirstRow }

	val regionText = if (region.isNotBlank() && country.isNotBlank())  "$region, $country" else suggestSecondRow
}
