package cz.cleevio.network.response.offer.v2

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OfferCreateRequestV2 constructor(
	val offerType: String,
	val expiration: Long,
	val payloadPublic: String,
	val offerPrivateList: List<OfferPrivateCreateV2>
)
