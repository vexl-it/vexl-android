package cz.cleevio.network.request.offer

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateOfferPrivatePartRequest constructor(
	val adminId: String,
	val privateParts: List<OfferPrivateCreateRequest>
)