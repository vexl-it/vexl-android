package cz.cleevio.repository.model.offer.v2

import cz.cleevio.network.response.offer.v2.OfferPrivateCreateV2

//increment when changing payload data model
const val PRIVATE_PAYLOAD_VERSION_PREFIX = "0"

data class NewOfferPrivateV2 constructor(
	val userPublicKey: String,
	val payloadPrivate: String
)

fun NewOfferPrivateV2.toNetworkV2(): OfferPrivateCreateV2 {
	return OfferPrivateCreateV2(
		userPublicKey = this.userPublicKey,
		payloadPrivate = PRIVATE_PAYLOAD_VERSION_PREFIX + this.payloadPrivate,
	)
}