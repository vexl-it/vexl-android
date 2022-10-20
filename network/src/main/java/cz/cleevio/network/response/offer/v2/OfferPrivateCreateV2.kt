package cz.cleevio.network.response.offer.v2

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OfferPrivateCreateV2 constructor(
	val userPublicKey: String,
	val payloadPrivate: String
)
