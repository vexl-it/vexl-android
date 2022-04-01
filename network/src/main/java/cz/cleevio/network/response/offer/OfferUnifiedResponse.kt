package cz.cleevio.network.response.offer

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OfferUnifiedResponse constructor(
	val id: Long,
	val location: String,
	val userPublicKey: String,
	val offerPublicKey: String,
	val direction: String,
	val premium: String,
	val threshold: String,
	val offerSymKey: String
)