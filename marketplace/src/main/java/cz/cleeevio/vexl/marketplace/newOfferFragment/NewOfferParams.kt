package cz.cleeevio.vexl.marketplace.newOfferFragment

import cz.cleevio.core.model.FeeValue
import cz.cleevio.core.model.LocationValue
import cz.cleevio.core.model.PriceRangeValue
import cz.cleevio.core.model.PriceTriggerValue

data class NewOfferParams(
	val location: LocationValue,    //todo: should contain GPS?
	val fee: FeeValue,
	val priceRange: PriceRangeValue,
	val priceTrigger: PriceTriggerValue    //todo: BE is missing field for this value
)