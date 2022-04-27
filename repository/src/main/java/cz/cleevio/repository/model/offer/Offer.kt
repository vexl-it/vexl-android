package cz.cleevio.repository.model.offer

import cz.cleevio.cache.entity.OfferEntity
import cz.cleevio.network.response.offer.OfferUnifiedResponse
import java.math.BigDecimal
import java.time.ZonedDateTime

data class Offer constructor(
	val id: Long = 0,
	val offerId: String,
	val location: String,
	val userPublicKey: String,
	val offerPublicKey: String,
	val offerDescription: String,
	val amountBottomLimit: BigDecimal,
	val amountTopLimit: BigDecimal,
	val feeState: String,
	val feeAmount: BigDecimal,
	val locationState: String,
	val paymentMethod: String,
	val btcNetwork: String,
	val friendLevel: String,
	val createdAt: ZonedDateTime,
	val modifiedAt: ZonedDateTime
)

fun OfferUnifiedResponse.fromNetwork(): Offer {
	return Offer(
		offerId = this.offerId.decryptedValue,
		location = this.location.decryptedValue,
		userPublicKey = this.userPublicKey,
		offerPublicKey = this.offerPublicKey.decryptedValue,
		offerDescription = this.offerDescription.decryptedValue,
		amountBottomLimit = this.amountBottomLimit.decryptedValue,
		amountTopLimit = this.amountTopLimit.decryptedValue,
		feeState = this.feeState.decryptedValue,
		feeAmount = this.feeAmount.decryptedValue,
		locationState = this.locationState.decryptedValue,
		paymentMethod = this.paymentMethod.decryptedValue,
		btcNetwork = this.btcNetwork.decryptedValue,
		friendLevel = this.friendLevel.decryptedValue,
		createdAt = ZonedDateTime.parse(this.createdAt.decryptedValue),
		modifiedAt = ZonedDateTime.parse(this.modifiedAt.decryptedValue)
	)
}

fun OfferEntity.fromCache(): Offer {
	return Offer(
		id = this.id,
		offerId = this.offerId,
		location = this.location,
		userPublicKey = this.userPublicKey,
		offerPublicKey = this.offerPublicKey,
		offerDescription = this.offerDescription,
		amountBottomLimit = this.amountBottomLimit,
		amountTopLimit = this.amountTopLimit,
		feeState = this.feeState,
		feeAmount = this.feeAmount,
		locationState = this.locationState,
		paymentMethod = this.paymentMethod,
		btcNetwork = this.btcNetwork,
		friendLevel = this.friendLevel,
		createdAt = this.createdAt,
		modifiedAt = this.modifiedAt
	)
}

fun Offer.toCache(): OfferEntity {
	return OfferEntity(
		id = this.id,
		offerId = this.offerId,
		location = this.location,
		userPublicKey = this.userPublicKey,
		offerPublicKey = this.offerPublicKey,
		offerDescription = this.offerDescription,
		amountBottomLimit = this.amountBottomLimit,
		amountTopLimit = this.amountTopLimit,
		feeState = this.feeState,
		feeAmount = this.feeAmount,
		locationState = this.locationState,
		paymentMethod = this.paymentMethod,
		btcNetwork = this.btcNetwork,
		friendLevel = this.friendLevel,
		createdAt = this.createdAt,
		modifiedAt = this.modifiedAt
	)
}