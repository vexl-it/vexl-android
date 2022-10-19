package cz.cleevio.core.utils

import android.app.Activity
import android.widget.Toast
import com.cleevio.vexl.cryptography.AesCryptoLib
import com.cleevio.vexl.cryptography.EciesCryptoLib
import com.cleevio.vexl.cryptography.model.KeyPair
import com.squareup.moshi.Moshi
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.R
import cz.cleevio.core.model.*
import cz.cleevio.core.widget.FriendLevel
import cz.cleevio.repository.model.contact.BaseContact
import cz.cleevio.repository.model.contact.ContactKey
import cz.cleevio.repository.model.contact.ContactLevel
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.model.offer.v2.NewOfferPrivateV2
import cz.cleevio.repository.model.offer.v2.NewOfferV2
import cz.cleevio.repository.model.offer.v2.NewOfferV2PrivatePayload
import cz.cleevio.repository.model.offer.v2.NewOfferV2PublicPayload
import cz.cleevio.repository.repository.contact.ContactRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

const val NO_GROUP = "NONE"

//increment when changing payload data model
const val PUBLIC_PAYLOAD_VERSION_PREFIX = "0"

class OfferUtils(
	private val moshi: Moshi
) {
	//sending just events, receiver is supposed to keep count, if count needed
	private val _offerWasEncrypted = Channel<Unit>(Channel.CONFLATED)
	val offerWasEncrypted = _offerWasEncrypted.receiveAsFlow()

	private val _numberOfAllContacts = MutableStateFlow(-1)
	val numberOfAllContacts = _numberOfAllContacts.asStateFlow()

	suspend fun fetchContactsPublicKeysV2(
		friendLevel: FriendLevel,
		groupUuids: List<String>,
		contactRepository: ContactRepository,
		encryptedPreferenceRepository: EncryptedPreferenceRepository,
		shouldEmitContacts: Boolean = false
	): List<ContactKey> {
		val contactsPublicKeys = when (friendLevel) {
			FriendLevel.NONE -> emptyList()
			FriendLevel.FIRST_DEGREE ->
				contactRepository.getFirstLevelContactKeys() +
					contactRepository.getGroupsContactKeys(groupUuids)
			FriendLevel.SECOND_DEGREE ->
				contactRepository.getFirstLevelContactKeys() +
					contactRepository.getSecondLevelContactKeys() +
					contactRepository.getGroupsContactKeys(groupUuids)
			else -> emptyList()
		}
			.toMutableList()
			.also {
				// Send info about number of contacts for which the offer should be encrypted into the bottom sheet dialog
				if (shouldEmitContacts) {
					_numberOfAllContacts.emit(it.size)
				}
				//also add user's key
				encryptedPreferenceRepository.userPublicKey.let { myPublicKey ->
					it.add(
						ContactKey(
							key = myPublicKey,
							level = ContactLevel.NOT_SPECIFIED,
							//fixme: this is just hotfix to have group uuid later, when we try to create additional private-parts
							groupUuid = groupUuids.firstOrNull(),
							isUpToDate = true
						)
					)
				}
			}

		//keep duplicities
		return contactsPublicKeys
	}

	suspend fun prepareEncryptedOfferV2(
		offerKeys: KeyPair,
		params: OfferParams,
		locationHelper: LocationHelper,
		contactsPublicKeys: List<ContactKey>,
		commonFriends: Map<String, List<BaseContact>>,
		symmetricalKey: String
	): NewOfferV2 {
		//encrypt public part
		val publicPayload = NewOfferV2PublicPayload(
			offerPublicKey = offerKeys.publicKey,
			location = params.location.values.map {
				locationHelper.locationToJsonString(it)
			},
			offerDescription = params.description,
			amountBottomLimit = params.priceRange.bottomLimit.toString(),
			amountTopLimit = params.priceRange.topLimit.toString(),
			feeState = params.fee.type.name,
			feeAmount = params.fee.value.toString(),
			locationState = params.location.type.name,
			paymentMethod = params.paymentMethod.value.map { it.name },
			btcNetwork = params.btcNetwork.value.map { it.name },
			currency = params.currency,
			offerType = params.offerType,
			activePriceState = params.priceTrigger.type.name,
			activePriceValue = params.priceTrigger.value.toString(),
			activePriceCurrency = params.priceTrigger.currency,
			active = params.active.toString(),
			groupUuids = params.groupUuids
		)
		val publicPayloadJson = moshi.adapter(NewOfferV2PublicPayload::class.java).toJson(publicPayload)
		val encryptedPublicPayload = AesCryptoLib.encrypt(symmetricalKey, publicPayloadJson)

		val encryptedPrivatePayloadList = encryptAllPrivatePayloads(
			contactsPublicKeys = contactsPublicKeys,
			commonFriends = commonFriends,
			symmetricalKey = symmetricalKey
		)

		return NewOfferV2(
			privateParts = encryptedPrivatePayloadList,
			payloadPublic = PUBLIC_PAYLOAD_VERSION_PREFIX + encryptedPublicPayload
		)
	}

	//encrypt offer from backgroundQueue (using already existing Offer)
	suspend fun prepareEncryptedOfferV2(
		offerKeys: KeyPair,
		offer: Offer,
		locationHelper: LocationHelper,
		contactsPublicKeys: List<ContactKey>,
		commonFriends: Map<String, List<BaseContact>>,
		symmetricalKey: String
	): NewOfferV2 {
		//encrypt public part
		val publicPayload = NewOfferV2PublicPayload(
			offerPublicKey = offerKeys.publicKey,
			location = offer.location.map {
				locationHelper.locationToJsonString(it)
			},
			offerDescription = offer.offerDescription,
			amountBottomLimit = offer.amountBottomLimit.toString(),
			amountTopLimit = offer.amountTopLimit.toString(),
			feeState = offer.feeState,
			feeAmount = offer.feeAmount.toString(),
			locationState = offer.locationState,
			paymentMethod = offer.paymentMethod,
			btcNetwork = offer.btcNetwork,
			currency = offer.currency,
			offerType = offer.offerType,
			activePriceState = offer.activePriceState,
			activePriceValue = offer.activePriceValue.toString(),
			activePriceCurrency = offer.activePriceCurrency,
			active = offer.active.toString(),
			groupUuids = offer.groupUuids
		)
		val publicPayloadJson = moshi.adapter(NewOfferV2PublicPayload::class.java).toJson(publicPayload)
		val encryptedPublicPayload = AesCryptoLib.encrypt(symmetricalKey, publicPayloadJson)

		val encryptedPrivatePayloadList = encryptAllPrivatePayloads(
			contactsPublicKeys = contactsPublicKeys,
			commonFriends = commonFriends,
			symmetricalKey = symmetricalKey
		)

		return NewOfferV2(
			privateParts = encryptedPrivatePayloadList,
			payloadPublic = PUBLIC_PAYLOAD_VERSION_PREFIX + encryptedPublicPayload
		)
	}

	suspend fun encryptAllPrivatePayloads(
		contactsPublicKeys: List<ContactKey>,
		commonFriends: Map<String, List<BaseContact>>,
		symmetricalKey: String
	): List<NewOfferPrivateV2> {
		//encrypt private parts
		val encryptedPrivatePayloadList: MutableList<NewOfferPrivateV2> = mutableListOf()

		val contactLevelMap: MutableMap<String, Set<ContactLevel>> = mutableMapOf()
		//we need to handle duplicities in list
		contactsPublicKeys.forEach {
			val existingLevel = contactLevelMap[it.key]?.toMutableSet() ?: mutableSetOf()
			existingLevel.add(it.level)
			contactLevelMap.replace(it.key, existingLevel)
		}

		contactsPublicKeys
			//remove duplicities
			.distinctBy { it.key }
			//encrypt for all
			.asyncAll { contactKey ->
				val encryptedOffer = encryptOfferPrivatePayload(
					contactKey = contactKey.key,
					// orEmpty should not happen, list in map is not nullable
					commonFriends = commonFriends[contactKey.key].orEmpty(),
					friendLevel = contactLevelMap[contactKey.key].orEmpty(),
					symmetricKey = symmetricalKey,
				)
				_offerWasEncrypted.send(Unit)
				encryptedPrivatePayloadList.add(encryptedOffer)
			}

		return encryptedPrivatePayloadList
	}

	fun encryptOfferPrivatePayload(
		contactKey: String,
		commonFriends: List<BaseContact>,
		friendLevel: Set<ContactLevel>,
		symmetricKey: String
	): NewOfferPrivateV2 {
		//encrypt private part
		val privatePayload = NewOfferV2PrivatePayload(
			commonFriends = commonFriends.map { friend -> friend.getHashedContact() },
			friendLevel = friendLevel.map { it.name },
			symetricKey = symmetricKey
		)
		val privatePayloadJson = moshi.adapter(NewOfferV2PrivatePayload::class.java).toJson(privatePayload)
		val encryptedPrivatePayload = eciesEncrypt(privatePayloadJson, contactKey)

		return NewOfferPrivateV2(
			userPublicKey = contactKey,
			payloadPrivate = encryptedPrivatePayload
		)
	}

	private fun eciesEncrypt(data: String, contactKey: String): String =
		EciesCryptoLib.encrypt(contactKey, data)

	@Suppress("ReturnCount", "LongMethod", "LongParameterList")
	fun isOfferParamsValid(
		activity: Activity,
		description: String,
		location: LocationValue,
		fee: FeeValue,
		priceRange: PriceRangeValue,
		friendLevel: FriendLevelValue,
		priceTrigger: PriceTriggerValue,
		btcNetwork: BtcNetworkValue,
		paymentMethod: PaymentMethodValue,
		offerType: String,
		expiration: Long,
		active: Boolean,
		currency: String = "CZK",
		groupUuids: List<String>
	): OfferParams? {
		if (description.isBlank()) {
			Toast.makeText(
				activity,
				activity.getString(R.string.error_missing_offer_description),
				Toast.LENGTH_SHORT
			).show()
			return null
		}
		if (location.values.isEmpty()) {
			Toast.makeText(
				activity,
				activity.getString(R.string.error_missing_offer_location),
				Toast.LENGTH_SHORT
			).show()
			return null
		}
		if (paymentMethod.value.isEmpty()) {
			Toast.makeText(
				activity,
				activity.getString(R.string.error_missing_offer_payment_method),
				Toast.LENGTH_SHORT
			).show()
			return null
		}
		if (priceTrigger.value == null) {
			Toast.makeText(
				activity,
				activity.getString(R.string.error_missing_offer_price_trigger),
				Toast.LENGTH_SHORT
			).show()
			return null
		}
		if (btcNetwork.value.isEmpty()) {
			Toast.makeText(
				activity,
				activity.getString(R.string.error_missing_offer_type),
				Toast.LENGTH_SHORT
			).show()
			return null
		}
		if (friendLevel.value == FriendLevel.NONE) {
			Toast.makeText(
				activity,
				activity.getString(R.string.error_missing_offer_friend_level),
				Toast.LENGTH_SHORT
			).show()
			return null
		}

		return OfferParams(
			description = description,
			location = location,
			fee = fee,
			priceRange = priceRange,
			priceTrigger = priceTrigger,
			paymentMethod = paymentMethod,
			btcNetwork = btcNetwork,
			friendLevel = friendLevel,
			offerType = offerType,
			expiration = expiration,
			active = active,
			currency = currency,
			groupUuids = groupUuids
		)
	}
}
