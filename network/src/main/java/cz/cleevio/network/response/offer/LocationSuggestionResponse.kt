package cz.cleevio.network.response.offer

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LocationSuggestionResponse constructor(
	@Json(name = "result") val result: List<LocationSuggestionResult>
)

@JsonClass(generateAdapter = true)
data class LocationSuggestionResult constructor(
	@Json(name = "userData") val userData: LocationSuggestionUserData
)

@JsonClass(generateAdapter = true)
data class LocationSuggestionUserData constructor(
	@Json(name = "municipality") val municipality: String,
	@Json(name = "region") val region: String,
	@Json(name = "country") val country: String,
	@Json(name = "latitude") val latitude: Double,
	@Json(name = "longitude") val longitude: Double
)
