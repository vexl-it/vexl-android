package cz.cleevio.repository.model.offer

import android.os.Parcelable
import cz.cleevio.cache.entity.ContactEntity
import cz.cleevio.cache.entity.LocationEntity
import cz.cleevio.cache.entity.OfferEntity
import cz.cleevio.network.response.offer.OfferUnifiedAdminResponse
import cz.cleevio.network.response.offer.OfferUnifiedResponse
import cz.cleevio.repository.model.contact.CommonFriend
import cz.cleevio.repository.model.contact.fromDao
import cz.cleevio.repository.repository.CryptoCurrencyUtils
import kotlinx.parcelize.Parcelize
import timber.log.Timber
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
	val activePriceCurrency: String,
	val active: Boolean,
	val commonFriends: List<CommonFriend>,
	val groupUuid: String,
	val currency: String,
	val createdAt: ZonedDateTime,
	val modifiedAt: ZonedDateTime,
	//custom flags
	var isMine: Boolean = false,
	var isRequested: Boolean = false
) : Parcelable {

	override fun toString(): String {
		return "\n\nOffer(databaseId=$databaseId,\nofferId='$offerId',\nlocation=$location,\nuserPublicKey='$userPublicKey',\nofferPublicKey='$offerPublicKey',\nofferDescription='$offerDescription',\namountBottomLimit=$amountBottomLimit,\namountTopLimit=$amountTopLimit,\nfeeState='$feeState',\nfeeAmount=$feeAmount,\nlocationState='$locationState',\npaymentMethod=$paymentMethod,\nbtcNetwork=$btcNetwork,\nfriendLevel='$friendLevel',\nofferType='$offerType',\nactivePriceState='$activePriceState',\nactivePriceValue=$activePriceValue,\nactive=$active,\ncommonFriends=$commonFriends,\ngroupUuid='$groupUuid',\ncurrency='$currency',\ncreatedAt=$createdAt,\nmodifiedAt=$modifiedAt,\nisMine=$isMine,\nisRequested=$isRequested)"
	}
}

fun OfferUnifiedResponse.fromNetwork(currencyUtils: CryptoCurrencyUtils): Offer {
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
		activePriceCurrency = this.activePriceCurrency.decryptedValue,
		active = this.active.decryptedValue.toBoolean(),
		groupUuid = this.groupUuid.decryptedValue,
		currency = this.currency.decryptedValue,
		commonFriends = this.commonFriends.map { CommonFriend(it.decryptedValue) },
		createdAt = ZonedDateTime.parse(this.createdAt, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")),
		modifiedAt = ZonedDateTime.parse(this.modifiedAt, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"))
	).let { offer ->
		Timber.tag("ASDX").d("${offer.activePriceState}")
		Timber.tag("ASDX").d("${offer.activePriceValue}")
		Timber.tag("ASDX").d("${offer.activePriceCurrency}")

		val currentCurrencyValue = currencyUtils.getPrice(offer.activePriceCurrency)
		if (offer.activePriceState == "PRICE_IS_ABOVE") {
			return@let offer.copy(active = offer.activePriceValue > currentCurrencyValue)
		}

		if (offer.activePriceState == "PRICE_IS_BELOW") {
			return@let offer.copy(active = offer.activePriceValue < currentCurrencyValue)
		}

		offer
	}
}

fun OfferUnifiedAdminResponse.fromNetwork(): Offer {
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
		activePriceCurrency = this.activePriceCurrency.decryptedValue,
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
		activePriceCurrency = this.activePriceCurrency,
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
		activePriceCurrency = this.activePriceCurrency,
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
		activePriceCurrency = this.activePriceCurrency,
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