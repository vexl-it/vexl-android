package cz.cleevio.network.response.offer

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OfferUnifiedResponse constructor(
	val offerId: String,
	val location: String,
	val userPublicKey: String,
	val offerPublicKey: String,
	val direction: String,
	val fee: String,
	val offerSymKey: String,
	val amount: String,
	val onlyInPerson: String,
	val paymentMethod: String,
	val typeNetwork: String,
	val friendLevel: String,
	val createdAt: String,
	val modifiedAt: String
)