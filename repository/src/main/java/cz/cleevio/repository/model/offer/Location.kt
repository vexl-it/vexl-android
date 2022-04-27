package cz.cleevio.repository.model.offer

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Location constructor(
	val longitude: Float,
	val latitude: Float,
	val radius: Float
)
