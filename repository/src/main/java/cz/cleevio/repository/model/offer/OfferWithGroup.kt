package cz.cleevio.repository.model.offer

import cz.cleevio.repository.model.group.Group

data class OfferWithGroup constructor(
	val offer: Offer,
	val group: Group?
)
