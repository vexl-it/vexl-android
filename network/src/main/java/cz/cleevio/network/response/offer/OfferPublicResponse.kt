package cz.cleevio.network.response.offer

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OfferPublicResponse constructor(
	val offerId: Long,
	val location: String,
	val offerPrivateResponseList: List<OfferPrivateResponse>
)

@JsonClass(generateAdapter = true)
data class OfferPrivateResponse constructor(
	val offerPrivatePartId: Long,
	val userPublicKey: String,
	val offerPublicKey: String,
	val direction: String,
	val premium: String,
	val threshold: String,
	val offerSymKey: String
)


