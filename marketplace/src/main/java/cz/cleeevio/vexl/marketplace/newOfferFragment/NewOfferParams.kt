package cz.cleeevio.vexl.marketplace.newOfferFragment

import cz.cleevio.core.model.*

data class NewOfferParams constructor(
	val description: String,
	val location: LocationValue,    //todo: should contain GPS?
	val fee: FeeValue,
	val priceRange: PriceRangeValue,
	val friendLevel: FriendLevelValue,
	val priceTrigger: PriceTriggerValue,    //todo: BE is missing field for this value
	val btcNetwork: BtcNetworkValue,
	val paymentMethod: PaymentMethodValue,
	val offerType: OfferTypeValue
)