package cz.cleevio.network.response.offer.v2

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OffersRefreshRequest constructor(
	val adminIds: List<String>
)
