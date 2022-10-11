package cz.cleevio.network.response.offer.v2

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateOfferRequestV2 constructor(
	val adminId: String,
	val payloadPublic: String,
	val offerPrivateList: List<OfferPrivateCreateV2>
)
