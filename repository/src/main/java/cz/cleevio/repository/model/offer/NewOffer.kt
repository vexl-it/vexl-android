package cz.cleevio.repository.model.offer

import cz.cleevio.network.request.offer.OfferPrivateCreateRequest

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
	val commonFriends: List<String>,
	val groupUuid: String,
	val currency: String,
)

fun NewOffer.toNetwork(): OfferPrivateCreateRequest {
	return OfferPrivateCreateRequest(
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
		groupUuid = this.groupUuid,
		currency = this.currency,
	)
}