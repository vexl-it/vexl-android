package cz.cleevio.network.request.offer

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateOfferRequest constructor(
	val location: String,
	val offerPrivateList: List<CreateOfferPrivateRequest>
)

@JsonClass(generateAdapter = true)
data class CreateOfferPrivateRequest constructor(
	val userPublicKey: String,
	val offerPublicKey: String,
	val direction: String,
	val premium: String,
	val threshold: String,
	val offerSymmetricKey: String
)
