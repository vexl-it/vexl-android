package cz.cleevio.repository.model.offer

import android.os.Parcelable
import com.cleevio.vexl.cryptography.AesCryptoLib
import com.cleevio.vexl.cryptography.EciesCryptoLib
import com.cleevio.vexl.cryptography.model.KeyPair
import com.squareup.moshi.Moshi
import cz.cleevio.cache.dao.ChatUserDao
import cz.cleevio.cache.entity.ContactEntity
import cz.cleevio.cache.entity.LocationEntity
import cz.cleevio.cache.entity.OfferEntity
import cz.cleevio.network.response.offer.v2.OfferUnifiedAdminResponseV2
import cz.cleevio.network.response.offer.v2.OfferUnifiedResponseV2
import cz.cleevio.repository.RandomUtils
import cz.cleevio.repository.model.chat.ChatUserIdentity
import cz.cleevio.repository.model.chat.fromCache
import cz.cleevio.repository.model.contact.CommonFriend
import cz.cleevio.repository.model.contact.fromDao
import cz.cleevio.repository.model.currency.CryptoCurrencyValues
import cz.cleevio.repository.model.offer.v2.NewOfferV2PrivatePayload
import cz.cleevio.repository.model.offer.v2.NewOfferV2PublicPayload
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// TODO Strings to enums
@Parcelize
@Suppress("DataClassShouldBeImmutable")
data class Offer constructor(
	val databaseId: Long = 0,
	val offerId: String,
	val location: List<Location>,
	//deprecated?
	val userPublicKey: String = "",
	val offerPublicKey: String,
	val offerDescription: String,
	val amountBottomLimit: BigDecimal,
	val amountTopLimit: BigDecimal,
	val feeState: String,
	val feeAmount: BigDecimal,
	val locationState: String,
	val paymentMethod: List<String>,
	val btcNetwork: List<String>,
	//possible values are from ContactLevel enum
	//	NONE, FIRST_DEGREE, SECOND_DEGREE, GROUP
	val friendLevel: List<String>,
	val offerType: String,
	val activePriceState: String,
	val activePriceValue: BigDecimal,
	val activePriceCurrency: String,
	val active: Boolean,
	val commonFriends: List<CommonFriend>,
	val groupUuids: List<String>,
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

@Suppress("LongMethod", "ReturnCount")
fun OfferUnifiedResponseV2.fromNetwork(moshi: Moshi, cryptoCurrencyValues: CryptoCurrencyValues?, reportedOfferIds: List<String>, keyPair: KeyPair): Offer? {
	val privatePayloadVersion = this.privatePayload.substring(0, 1)
	val privatePayloadData = this.privatePayload.substring(1)
	val privatePayloadJson = when (privatePayloadVersion) {
		"0" -> {
			val decrypted = EciesCryptoLib.decrypt(keyPair, privatePayloadData)
			Timber.tag("CRASH").d("privatePayloadData decrypted: ${decrypted}")
			moshi.adapter(NewOfferV2PrivatePayload::class.java).fromJson(decrypted)!!
		}
		else -> {
			return null
		}
	}

	val publicPayloadVersion = this.publicPayload.substring(0, 1)
	val publicPayloadData = this.publicPayload.substring(1)
	val publicPayloadDecrypted = when (publicPayloadVersion) {
		"0" -> {
			AesCryptoLib.decrypt(privatePayloadJson.symmetricKey, publicPayloadData)
		}
		else -> {
			return null
		}
	}

	Timber.tag("CRASH").d("publicPayloadDecrypted: ${publicPayloadDecrypted}")
	val publicPayloadJson = moshi.adapter(NewOfferV2PublicPayload::class.java).fromJson(publicPayloadDecrypted)!!
	val locationAdapter = moshi.adapter(Location::class.java)

	return Offer(
		offerId = this.offerId,
		location = publicPayloadJson.location.map { locationAdapter.fromJson(it)!! },
		offerPublicKey = publicPayloadJson.offerPublicKey,
		offerDescription = publicPayloadJson.offerDescription,
		amountBottomLimit = publicPayloadJson.amountBottomLimit.toBigDecimal(),
		amountTopLimit = publicPayloadJson.amountTopLimit.toBigDecimal(),
		feeState = publicPayloadJson.feeState,
		feeAmount = publicPayloadJson.feeAmount.toBigDecimal(),
		locationState = publicPayloadJson.locationState,
		paymentMethod = publicPayloadJson.paymentMethod,
		btcNetwork = publicPayloadJson.btcNetwork,
		friendLevel = privatePayloadJson.friendLevel,
		offerType = publicPayloadJson.offerType,
		activePriceState = publicPayloadJson.activePriceState,
		activePriceValue = publicPayloadJson.activePriceValue.toBigDecimal(),
		activePriceCurrency = publicPayloadJson.activePriceCurrency,
		active = publicPayloadJson.active.toBoolean(),
		groupUuids = publicPayloadJson.groupUuids,
		currency = publicPayloadJson.currency,
		commonFriends = privatePayloadJson.commonFriends.map { CommonFriend(it) },
		createdAt = ZonedDateTime.parse(this.createdAt, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")),
		modifiedAt = ZonedDateTime.parse(this.modifiedAt, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"))
	).let { unprocessedOffer ->

		var offer = unprocessedOffer
		// if we have paused offer (active == false), skip trigger code block
		if (cryptoCurrencyValues != null && offer.active) {
			val currentCurrencyValue = cryptoCurrencyValues.getPrice(offer.activePriceCurrency)
			if (offer.activePriceState == PriceTriggerType.PRICE_IS_ABOVE.name) {
				offer = offer.copy(active = currentCurrencyValue > offer.activePriceValue)
			}

			if (offer.activePriceState == PriceTriggerType.PRICE_IS_BELOW.name) {
				offer = offer.copy(active = currentCurrencyValue < offer.activePriceValue)
			}
		}

		// if we have paused offer (active == false), skip reported offer block
		if (offer.active) {
			//reported offers should not be active
			if (reportedOfferIds.contains(offer.offerId)) {
				offer = offer.copy(active = false)
			}
		}

		offer
	}
}

@Suppress("ReturnCount")
fun OfferUnifiedAdminResponseV2.fromNetwork(moshi: Moshi, keyPair: KeyPair): Offer? {
	val privatePayloadVersion = this.privatePayload.substring(0, 1)
	val privatePayloadData = this.privatePayload.substring(1)
	val privatePayloadJson = when (privatePayloadVersion) {
		"0" -> {
			val decrypted = EciesCryptoLib.decrypt(keyPair, privatePayloadData)
			Timber.tag("CRASH").d("privatePayloadData decrypted: ${decrypted}")
			moshi.adapter(NewOfferV2PrivatePayload::class.java).fromJson(decrypted)!!
		}
		else -> {
			return null
		}
	}

	val publicPayloadVersion = this.publicPayload.substring(0, 1)
	val publicPayloadData = this.publicPayload.substring(1)
	val publicPayloadDecrypted = when (publicPayloadVersion) {
		"0" -> {
			AesCryptoLib.decrypt(privatePayloadJson.symmetricKey, publicPayloadData)
		}
		else -> {
			return null
		}
	}

	Timber.tag("CRASH").d("publicPayloadDecrypted: ${publicPayloadDecrypted}")
	val publicPayloadJson = moshi.adapter(NewOfferV2PublicPayload::class.java).fromJson(publicPayloadDecrypted)!!
	val locationAdapter = moshi.adapter(Location::class.java)

	return Offer(
		offerId = this.offerId,
		location = publicPayloadJson.location.map { locationAdapter.fromJson(it)!! },
		offerPublicKey = publicPayloadJson.offerPublicKey,
		offerDescription = publicPayloadJson.offerDescription,
		amountBottomLimit = publicPayloadJson.amountBottomLimit.toBigDecimal(),
		amountTopLimit = publicPayloadJson.amountTopLimit.toBigDecimal(),
		feeState = publicPayloadJson.feeState,
		feeAmount = publicPayloadJson.feeAmount.toBigDecimal(),
		locationState = publicPayloadJson.locationState,
		paymentMethod = publicPayloadJson.paymentMethod,
		btcNetwork = publicPayloadJson.btcNetwork,
		friendLevel = privatePayloadJson.friendLevel,
		offerType = publicPayloadJson.offerType,
		activePriceState = publicPayloadJson.activePriceState,
		activePriceValue = publicPayloadJson.activePriceValue.toBigDecimal(),
		activePriceCurrency = publicPayloadJson.activePriceCurrency,
		active = publicPayloadJson.active.toBoolean(),
		groupUuids = publicPayloadJson.groupUuids,
		currency = publicPayloadJson.currency,
		commonFriends = privatePayloadJson.commonFriends.map { CommonFriend(it) },
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
		friendLevel = this.friendLevel.split(",").map { it.trim() },
		offerType = this.offerType,
		activePriceState = this.activePriceState,
		activePriceValue = this.activePriceValue,
		activePriceCurrency = this.activePriceCurrency,
		active = this.active,
		groupUuids = this.groupUuid.split(",").map { it.trim() },
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
			offer.fillUserInfo(chatUserIdentity)
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
		friendLevel = this.friendLevel.split(",").map { it.trim() },
		offerType = this.offerType,
		activePriceState = this.activePriceState,
		activePriceValue = this.activePriceValue,
		activePriceCurrency = this.activePriceCurrency,
		active = this.active,
		groupUuids = this.groupUuid.split(",").map { it.trim() },
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
		friendLevel = this.friendLevel.joinToString(),
		offerType = this.offerType,
		activePriceState = this.activePriceState,
		activePriceValue = this.activePriceValue,
		activePriceCurrency = this.activePriceCurrency,
		active = this.active,
		commonFriends = this.commonFriends.joinToString(),
		groupUuid = this.groupUuids.joinToString(),
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
	this.userAvatar = chatUserIdentity.avatarBase64
}