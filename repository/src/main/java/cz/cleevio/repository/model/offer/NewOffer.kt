package cz.cleevio.repository.model.offer

import cz.cleevio.network.request.offer.CreateOfferPrivateRequest

//all fields should be encrypted strings
data class NewOffer constructor(
	val userPublicKey: String,
	val offerPublicKey: String,
	val location: List<String>,
	val offerDescription: String,
	val amountBottomLimit: String,
	val amountTopLimit: String,
	val feeState: String,
	val feeAmount: String,
	val locationState: String,
	val paymentMethod: List<String>,
	val btcNetwork: List<String>,
	val friendLevel: String,
	val offerType: String,
	val activePriceState: String,
	val activePriceValue: String,
	val active: String,
	//fixme: common friends will be implemented later
	val commonFriends: List<String> = listOf(),
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
		friendLevel = this.friendLevel,
		offerType = this.offerType,
		activePriceState = this.activePriceState,
		activePriceValue = this.activePriceValue,
		active = this.active,
		commonFriends = this.commonFriends,
	)
}
