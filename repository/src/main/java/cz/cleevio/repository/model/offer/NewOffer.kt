package cz.cleevio.repository.model.offer

import cz.cleevio.network.request.offer.CreateOfferPrivateRequest

//all fields should be encrypted strings
data class NewOffer constructor(
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

fun NewOffer.toNetwork(): CreateOfferPrivateRequest {
	return CreateOfferPrivateRequest(
		location = this.location,
		userPublicKey = this.userPublicKey,
		offerPublicKey = this.offerPublicKey,
		direction = this.direction,
		fee = this.fee,
		amount = this.amount,
		onlyInPerson = this.onlyInPerson,
		paymentMethod = this.paymentMethod,
		typeNetwork = this.typeNetwork,
		friendLevel = this.friendLevel,
		offerSymmetricKey = this.offerSymmetricKey
	)
}
