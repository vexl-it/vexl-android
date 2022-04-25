package cz.cleevio.network.request.offer

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateOfferRequest constructor(
	val offerPrivateList: List<CreateOfferPrivateRequest>
)

@JsonClass(generateAdapter = true)
data class CreateOfferPrivateRequest constructor(
	val location: String,
	val userPublicKey: String,
	val offerPublicKey: String,
	val direction: String,
	val fee: String,
	val amount: String,
	val onlyInPerson: String,
	val paymentMethod: String,
	val typeNetwork: String,
	val friendLevel: String,
	val offerSymmetricKey: String
)
