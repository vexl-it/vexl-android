package cz.cleevio.repository.model.offer

data class LocationSuggestion constructor(
	val city: String,
	val region: String,
	val country: String
) {
	override fun toString() = city
}
