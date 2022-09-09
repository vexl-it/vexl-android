package cz.cleevio.repository.model.offer

import android.os.Parcelable
import cz.cleevio.cache.dao.ChatUserDao
import cz.cleevio.cache.entity.ContactEntity
import cz.cleevio.cache.entity.LocationEntity
import cz.cleevio.cache.entity.OfferEntity
import cz.cleevio.network.response.offer.OfferUnifiedAdminResponse
import cz.cleevio.network.response.offer.OfferUnifiedResponse
import cz.cleevio.repository.RandomUtils
import cz.cleevio.repository.model.chat.ChatUserIdentity
import cz.cleevio.repository.model.chat.fromCache
import cz.cleevio.repository.model.contact.CommonFriend
import cz.cleevio.repository.model.contact.fromDao
import cz.cleevio.repository.model.currency.CryptoCurrencyValues
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
	var isRequested: Boolean = false,
	var userName: String? = null,
	var userAvatar: String? = null,
	var userAvatarId: Int? = null,
) : Parcelable

fun OfferUnifiedResponse.fromNetwork(cryptoCurrencyValues: CryptoCurrencyValues?): Offer {
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

		if (cryptoCurrencyValues != null) {
			val currentCurrencyValue = cryptoCurrencyValues.getPrice(offer.activePriceCurrency)
			if (offer.activePriceState == PriceTriggerType.PRICE_IS_ABOVE.name) {
				return@let offer.copy(active = currentCurrencyValue > offer.activePriceValue)
			}

			if (offer.activePriceState == PriceTriggerType.PRICE_IS_BELOW.name) {
				return@let offer.copy(active = currentCurrencyValue < offer.activePriceValue)
			}
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

fun OfferEntity.fromCache(locations: List<LocationEntity>, commonFriends: List<ContactEntity>, chatUserDao: ChatUserDao): Offer {
	val offer = Offer(
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

	if (!offer.isMine) {
		//try to find saved user name and photo and stuff
		val chatUserIdentity = chatUserDao.getUserByContactKey(offer.offerPublicKey)?.fromCache()
		//we expect it to be already in DB from syncOffers
		if (chatUserIdentity != null) {
			Timber.tag("ASDX").d("chatUserIdentity anonymousAvatarImageIndex ${chatUserIdentity.anonymousAvatarImageIndex}")
			offer.fillUserInfo(chatUserIdentity)
		} else {
			Timber.tag("ASDX").d("chatUserIdentity is null for key: ${offer.offerPublicKey}")
		}
	}

	return offer
}

fun OfferEntity.fromCacheWithoutFriendsMapping(
	locations: List<LocationEntity>, commonFriends: List<CommonFriend>,
	chatUserDao: ChatUserDao, chatUserKey: String? = null, inboxKey: String? = null
): Offer {
	val offer = Offer(
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

	//try to find saved user name and photo and stuff
	val chatUserIdentity = chatUserDao.getUserIdentity(
		inboxKey = inboxKey ?: offer.offerPublicKey,
		contactPublicKey = chatUserKey ?: offer.offerPublicKey
	)?.fromCache()
	//we expect it to be already in DB from syncOffers for offers that are not mine
	//and from syncMessages with type REQUEST_MESSAGING for mine offers
	if (chatUserIdentity != null) {
		offer.fillUserInfo(chatUserIdentity)
	}

	return offer
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

private fun Offer.fillUserInfo(chatUserIdentity: ChatUserIdentity) {
	this.userName = if (chatUserIdentity.name.isNullOrBlank()) {
		chatUserIdentity.anonymousUsername
	} else {
		chatUserIdentity.name
	}
	this.userAvatarId = RandomUtils.getRandomImageDrawableId(chatUserIdentity.anonymousAvatarImageIndex ?: 0)
	Timber.tag("ASDX").d("this.userAvatarId ${this.userAvatarId}")
	this.userAvatar = chatUserIdentity.avatar
}