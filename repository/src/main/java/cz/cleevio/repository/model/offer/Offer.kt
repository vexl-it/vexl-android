package cz.cleevio.repository.model.offer

import cz.cleevio.network.response.offer.OfferUnifiedResponse

data class Offer constructor(
	val id: Long,
	val location: String,
	val userPublicKey: String,
	val offerPublicKey: String,
	val direction: String,
	val premium: String,
	val threshold: String,
	val offerSymKey: String
)

fun OfferUnifiedResponse.fromNetwork(): Offer {
	return Offer(
		id = this.id,
		location = this.location,
		userPublicKey = this.userPublicKey,
		offerPublicKey = this.offerPublicKey,
		direction = this.direction,
		premium = this.premium,
		threshold = this.threshold,
		offerSymKey = this.offerSymKey
	)
}