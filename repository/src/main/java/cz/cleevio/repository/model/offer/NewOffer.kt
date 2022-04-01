package cz.cleevio.repository.model.offer

import cz.cleevio.network.request.offer.CreateOfferPrivateRequest

//all fields should be encrypted strings
data class NewOffer constructor(
	val userPublicKey: String,
	val offerPublicKey: String,
	val direction: String,
	val premium: String,
	val threshold: String,
	val offerSymmetricKey: String
)

fun NewOffer.toNetwork(): CreateOfferPrivateRequest {
	return CreateOfferPrivateRequest(
		userPublicKey = this.userPublicKey,
		offerPublicKey = this.offerPublicKey,
		direction = this.direction,
		premium = this.premium,
		threshold = this.threshold,
		offerSymmetricKey = this.offerSymmetricKey
	)
}
