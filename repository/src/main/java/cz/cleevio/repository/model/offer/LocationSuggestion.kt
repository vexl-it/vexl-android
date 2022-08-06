package cz.cleevio.repository.model.offer

import java.math.BigDecimal

data class LocationSuggestion constructor(
	val city: String,
	val region: String,
	val country: String,
	val latitude: BigDecimal,
	val longitude: BigDecimal
) {
	override fun toString() = city
}
