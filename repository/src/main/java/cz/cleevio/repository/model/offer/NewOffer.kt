package cz.cleevio.repository.model.offer

import cz.cleevio.network.request.offer.CreateOfferPrivateRequest

//all fields should be encrypted strings
data class NewOffer constructor(
	val userPublicKey: String = "",
	val offerPublicKey: String = "",
	val location: List<String> = listOf(),
	val offerDescription: String = "",
	val amountBottomLimit: String = "",
	val amountTopLimit: String = "",
	val feeState: String = "",
	val feeAmount: String = "",
	val locationState: String = "",
	val paymentMethod: List<String> = listOf(),
	val btcNetwork: List<String> = listOf(),
	val friendLevel: String = ""
)

fun NewOffer.toNetwork(): CreateOfferPrivateRequest {
	return CreateOfferPrivateRequest(
		userPublicKey = this.userPublicKey,
		offerPublicKey = this.offerPublicKey,
		location = this.location,
		offerDescription = this.offerDescription,
		amountBottomLimit = this.amountBottomLimit,
		amountTopLimit = this.amountTopLimit,
		feeState = this.feeState,
		feeAmount = this.feeAmount,
		locationState = this.locationState,
		paymentMethod = this.paymentMethod,
		btcNetwork = this.btcNetwork,
		friendLevel = this.friendLevel
	)
}
