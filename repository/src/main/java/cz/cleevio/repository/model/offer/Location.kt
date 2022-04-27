package cz.cleevio.repository.model.offer


data class Location constructor(
	val longitude: Float,
	val latitude: Float,
	val radius: Float
)

fun Location.toJsonString(): String {
	//todo: to JSON

	//todo: to String
	return "temp-location"
}
