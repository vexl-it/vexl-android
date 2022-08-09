package cz.cleevio.repository.model.offer

import android.os.Parcelable
import cz.cleevio.cache.entity.ContactEntity
import cz.cleevio.cache.entity.LocationEntity
import cz.cleevio.cache.entity.OfferEntity
import cz.cleevio.network.response.offer.OfferUnifiedResponse
import cz.cleevio.repository.model.contact.CommonFriend
import cz.cleevio.repository.model.contact.fromDao
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Parcelize
@Suppress("DataClassShouldBeImmutable")
data class Offer constructor(
	val databaseId: Long = 0,
	val offerId: String,
	val location: List<Location>,
	val userPublicKey: String,
	val offerPublicKey: String,
	val offerDescription: String,
	val amountBottomLimit: BigDecimal,
	val amountTopLimit: BigDecimal,
	val feeState: String,
	val feeAmount: BigDecimal,
	val locationState: String,
	val paymentMethod: List<String>,
	val btcNetwork: List<String>,
	val friendLevel: String,
	val offerType: String,
	val activePriceState: String,
	val activePriceValue: BigDecimal,
	val active: Boolean,
	val commonFriends: List<CommonFriend>,
	val groupUuid: String,
	val currency: String,
	val createdAt: ZonedDateTime,
	val modifiedAt: ZonedDateTime,
	//custom flags
	var isMine: Boolean = false,
	var isRequested: Boolean = false
) : Parcelable

fun OfferUnifiedResponse.fromNetwork(): Offer {
	return Offer(
		offerId = this.offerId,
		location = this.location.map { it.decryptedValue.fromNetwork() },
		userPublicKey = this.userPublicKey,
		offerPublicKey = this.offerPublicKey.decryptedValue,
		offerDescription = this.offerDescription.decryptedValue,
		amountBottomLimit = this.amountBottomLimit.decryptedValue,
		amountTopLimit = this.amountTopLimit.decryptedValue,
		feeState = this.feeState.decryptedValue,
		feeAmount = this.feeAmount.decryptedValue,
		locationState = this.locationState.decryptedValue,
		paymentMethod = this.paymentMethod.map { it.decryptedValue },
		btcNetwork = this.btcNetwork.map { it.decryptedValue },
		friendLevel = this.friendLevel.decryptedValue,
		offerType = this.offerType.decryptedValue,
		activePriceState = this.activePriceState.decryptedValue,
		activePriceValue = this.activePriceValue.decryptedValue,
		active = this.active.decryptedValue.toBoolean(),
		groupUuid = this.groupUuid.decryptedValue,
		currency = this.currency.decryptedValue,
		commonFriends = this.commonFriends.map { CommonFriend(it.decryptedValue) },
		createdAt = ZonedDateTime.parse(this.createdAt, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")),
		modifiedAt = ZonedDateTime.parse(this.modifiedAt, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"))
	)
}

fun OfferEntity.fromCache(locations: List<LocationEntity>, commonFriends: List<ContactEntity>): Offer {
	return Offer(
		databaseId = this.offerId,
		offerId = this.externalOfferId,
		location = locations.map { it.fromCache() },
		userPublicKey = this.userPublicKey,
		offerPublicKey = this.offerPublicKey,
		offerDescription = this.offerDescription,
		amountBottomLimit = this.amountBottomLimit,
		amountTopLimit = this.amountTopLimit,
		feeState = this.feeState,
		feeAmount = this.feeAmount,
		locationState = this.locationState,
		paymentMethod = this.paymentMethod.split(",").map { it.trim() },
		btcNetwork = this.btcNetwork.split(",").map { it.trim() },
		friendLevel = this.friendLevel,
		offerType = this.offerType,
		activePriceState = this.activePriceState,
		activePriceValue = this.activePriceValue,
		active = this.active,
		groupUuid = this.groupUuid,
		currency = this.currency,
		commonFriends = commonFriends.map {
			val contact = it.fromDao()
			CommonFriend(contact.getHashedContact(), contact)
		},
		createdAt = this.createdAt,
		modifiedAt = this.modifiedAt,
		isMine = this.isMine,
		isRequested = this.isRequested
	)
}

fun OfferEntity.fromCacheWithoutFriendsMapping(locations: List<LocationEntity>, commonFriends: List<CommonFriend>): Offer {
	return Offer(
		databaseId = this.offerId,
		offerId = this.externalOfferId,
		location = locations.map { it.fromCache() },
		userPublicKey = this.userPublicKey,
		offerPublicKey = this.offerPublicKey,
		offerDescription = this.offerDescription,
		amountBottomLimit = this.amountBottomLimit,
		amountTopLimit = this.amountTopLimit,
		feeState = this.feeState,
		feeAmount = this.feeAmount,
		locationState = this.locationState,
		paymentMethod = this.paymentMethod.split(",").map { it.trim() },
		btcNetwork = this.btcNetwork.split(",").map { it.trim() },
		friendLevel = this.friendLevel,
		offerType = this.offerType,
		activePriceState = this.activePriceState,
		activePriceValue = this.activePriceValue,
		active = this.active,
		groupUuid = this.groupUuid,
		currency = this.currency,
		commonFriends = commonFriends,
		createdAt = this.createdAt,
		modifiedAt = this.modifiedAt,
		isMine = this.isMine,
		isRequested = this.isRequested
	)
}

fun Offer.toCache(): OfferEntity {
	return OfferEntity(
		offerId = this.databaseId,
		externalOfferId = this.offerId,
		userPublicKey = this.userPublicKey,
		offerPublicKey = this.offerPublicKey,
		offerDescription = this.offerDescription,
		amountBottomLimit = this.amountBottomLimit,
		amountTopLimit = this.amountTopLimit,
		feeState = this.feeState,
		feeAmount = this.feeAmount,
		locationState = this.locationState,
		paymentMethod = this.paymentMethod.joinToString(),
		btcNetwork = this.btcNetwork.joinToString(),
		friendLevel = this.friendLevel,
		offerType = this.offerType,
		activePriceState = this.activePriceState,
		activePriceValue = this.activePriceValue,
		active = this.active,
		commonFriends = this.commonFriends.joinToString(),
		groupUuid = this.groupUuid,
		currency = this.currency,
		createdAt = this.createdAt,
		modifiedAt = this.modifiedAt,
		isMine = this.isMine,
		isRequested = this.isRequested
	)
}