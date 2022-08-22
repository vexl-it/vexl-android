package cz.cleevio.network.request.offer

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateOfferRequest constructor(
	val adminId: String,
	val offerPrivateCreateList: List<OfferPrivateCreateRequest>
)
