package cz.cleevio.repository.model.offer

import cz.cleevio.network.response.offer.OfferUnifiedResponse
import java.time.ZonedDateTime

data class Offer constructor(
	val offerId: Long,
	val location: String,
	val userPublicKey: String,
	val offerPublicKey: String,
	val direction: String,
	val fee: String?,
	val offerSymKey: String,
	val amount: String?,
	val onlyInPerson: String,
	val paymentMethod: String,
	val typeNetwork: String,
	val friendLevel: String,
	val createdAt: ZonedDateTime,
	val modifiedAt: ZonedDateTime
)

fun OfferUnifiedResponse.fromNetwork(): Offer {
	return Offer(
		// TODO u sure toLong()?
		offerId = this.offerId.toLong(),
		location = this.location,
		userPublicKey = this.userPublicKey,
		offerPublicKey = this.offerPublicKey,
		direction = this.direction,
		fee = this.fee,
		offerSymKey = this.offerSymKey,
		amount = this.amount,
		onlyInPerson = this.onlyInPerson,
		paymentMethod = this.paymentMethod,
		typeNetwork = this.typeNetwork,
		friendLevel = this.friendLevel,
		createdAt = ZonedDateTime.parse(this.createdAt),
		modifiedAt = ZonedDateTime.parse(this.modifiedAt)
	)
}