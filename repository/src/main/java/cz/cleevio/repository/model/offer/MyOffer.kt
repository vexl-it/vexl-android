package cz.cleevio.repository.model.offer

import cz.cleevio.cache.entity.MyOfferEntity

data class MyOffer constructor(
	val offerId: String,
	val adminId: String,
	val privateKey: String,
	val publicKey: String,
	val offerType: String,
	val isInboxCreated: Boolean,
	//list of public keys for which we have already encrypted my offer
	val encryptedFor: List<String>,
	//symmetrical key for public payload
	val symmetricalKey: String,
	//friend level selected in UI
	val friendLevel: String,
)

fun MyOfferEntity.fromCache(): MyOffer = MyOffer(
	offerId = this.extId,
	adminId = this.adminId,
	privateKey = this.privateKey,
	publicKey = this.publicKey,
	offerType = this.offerType,
	isInboxCreated = this.isInboxCreated,
	encryptedFor = this.encryptedForKeys.split(",").map { it.trim() },
	symmetricalKey = this.symmetricalKey,
	friendLevel = this.friendLevel,
)

fun MyOffer.toCache(): MyOfferEntity = MyOfferEntity(
	extId = this.offerId,
	adminId = this.adminId,
	privateKey = this.privateKey,
	publicKey = this.publicKey,
	offerType = this.offerType,
	isInboxCreated = this.isInboxCreated,
	encryptedForKeys = this.encryptedFor.joinToString(),
	symmetricalKey = this.symmetricalKey,
	friendLevel = this.friendLevel,
)