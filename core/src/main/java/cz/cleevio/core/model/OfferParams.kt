package cz.cleevio.core.model

data class OfferParams constructor(
	val description: String,
	//todo: should contain GPS?
	val location: LocationValue,
	val fee: FeeValue,
	val priceRange: PriceRangeValue,
	val friendLevel: FriendLevelValue,
	//todo: BE is missing field for this value
	val priceTrigger: PriceTriggerValue,
	val btcNetwork: BtcNetworkValue,
	val paymentMethod: PaymentMethodValue,
	val offerType: String
)