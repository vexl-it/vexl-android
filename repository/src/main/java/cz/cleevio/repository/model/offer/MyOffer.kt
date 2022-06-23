package cz.cleevio.repository.model.offer

import cz.cleevio.cache.entity.MyOfferEntity

data class MyOffer constructor(
	val offerId: String,
	val privateKey: String,
	val publicKey: String,
	val offerType: String,
	val isInboxCreated: Boolean,
)

fun MyOfferEntity.fromCache(): MyOffer = MyOffer(
	offerId = this.extId,
	privateKey = this.privateKey,
	publicKey = this.publicKey,
	offerType = this.offerType,
	isInboxCreated = this.isInboxCreated
)