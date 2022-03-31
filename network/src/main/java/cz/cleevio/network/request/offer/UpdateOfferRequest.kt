package cz.cleevio.network.request.offer

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateOfferRequest constructor(
	val offerId: Long,
	val location: String,
	val offerPrivateUpdates: List<OfferPrivateUpdateRequest>
)

@JsonClass(generateAdapter = true)
data class OfferPrivateUpdateRequest constructor(
	val id: Long,
	val direction: String,
	val premium: String,
	val threshold: String
)
