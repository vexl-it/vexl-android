package cz.cleevio.core.utils

import android.app.Activity
import android.widget.Toast
import com.cleevio.vexl.cryptography.EciesCryptoLib
import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.R
import cz.cleevio.core.model.*
import cz.cleevio.core.widget.FriendLevel
import cz.cleevio.repository.model.contact.BaseContact
import cz.cleevio.repository.model.contact.ContactKey
import cz.cleevio.repository.model.contact.ContactLevel
import cz.cleevio.repository.model.offer.NewOffer
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.repository.contact.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object OfferUtils {

	private const val NO_GROUP = "NONE"

	private val _offerWasEncryptedForNumberOfContacts = MutableStateFlow(0)
	val offerWasEncryptedForNumberOfContacts = _offerWasEncryptedForNumberOfContacts.asStateFlow()

	private val _numberOfAllContacts = MutableStateFlow(-1)
	val numberOfAllContacts = _numberOfAllContacts.asStateFlow()

	suspend fun prepareEncryptedOffers(
		offerKeys: KeyPair,
		params: OfferParams,
		contactRepository: ContactRepository,
		encryptedPreferenceRepository: EncryptedPreferenceRepository,
		locationHelper: LocationHelper
	): List<NewOffer> {
		val encryptedOfferList: MutableList<NewOffer> = mutableListOf()

		//load all public keys for specified level of friends
		val contactsPublicKeys = when (params.friendLevel.value) {
			FriendLevel.NONE -> emptyList()
			FriendLevel.FIRST_DEGREE ->
				contactRepository.getFirstLevelContactKeys() +
					contactRepository.getGroupsContactKeys(params.groupUuids)
			FriendLevel.SECOND_DEGREE ->
				contactRepository.getFirstLevelContactKeys() +
					contactRepository.getSecondLevelContactKeys() +
					contactRepository.getGroupsContactKeys(params.groupUuids)
			else -> emptyList()
		}
			.toMutableList()
			.also {
				// Send info about number of contacts for which the offer should be encrypted into the bottom sheet dialog
				_numberOfAllContacts.emit(it.size)
				//also add user's key
				encryptedPreferenceRepository.userPublicKey.let { myPublicKey ->
					it.add(
						ContactKey(
							key = myPublicKey,
							level = ContactLevel.NOT_SPECIFIED,
							//fixme: this is just hotfix to have group uuid later, when we try to create additional private-parts
							groupUuid = params.groupUuids.firstOrNull(),
							isUpToDate = true
						)
					)
				}
			}
			//we need to give keys with group priority, because distinctBy keeps only first element
			.sortedBy {
				if (it.groupUuid == null) 1 else 0
			}
			.distinctBy {
				// remove duplicities
				it.key
			}

		val commonFriends = contactRepository.getCommonFriends(
			contactsPublicKeys
				.map { it.key }
				.filter {
					// we don't want to get common friends for our offer
					it != encryptedPreferenceRepository.userPublicKey
				}
		)

		//encrypt in loop for every contact
		contactsPublicKeys.forEachIndexed { index, contactKey ->
			val encryptedOffer = encryptOffer(
				locationHelper = locationHelper,
				params = params,
				// orEmpty should not happen, list in map is not nullable
				commonFriends = commonFriends[contactKey.key].orEmpty(),
				contactKey = contactKey.key,
				offerKeys = offerKeys,
				groupUuid = contactKey.groupUuid
			)
			_offerWasEncryptedForNumberOfContacts.emit(index)
			encryptedOfferList.add(encryptedOffer)
		}

		return encryptedOfferList
	}

	fun encryptOffer(
		locationHelper: LocationHelper,
		params: OfferParams,
		commonFriends: List<BaseContact>,
		contactKey: String,
		offerKeys: KeyPair,
		groupUuid: String?,
	): NewOffer {
		return NewOffer(
			location = params.location.values.map {
				eciesEncrypt(locationHelper.locationToJsonString(it), contactKey)
			},
			userPublicKey = contactKey,
			offerPublicKey = eciesEncrypt(offerKeys.publicKey, contactKey),
			feeState = eciesEncrypt(params.fee.type.name, contactKey),
			feeAmount = eciesEncrypt(params.fee.value.toString(), contactKey),
			offerDescription = eciesEncrypt(params.description, contactKey),
			amountBottomLimit = eciesEncrypt(params.priceRange.bottomLimit.toString(), contactKey),
			amountTopLimit = eciesEncrypt(params.priceRange.topLimit.toString(), contactKey),
			locationState = eciesEncrypt(params.location.type.name, contactKey),
			paymentMethod = params.paymentMethod.value.map { eciesEncrypt(it.name, contactKey) },
			btcNetwork = params.btcNetwork.value.map { eciesEncrypt(it.name, contactKey) },
			friendLevel = eciesEncrypt(params.friendLevel.value.name, contactKey),
			offerType = eciesEncrypt(params.offerType, contactKey),
			activePriceState = eciesEncrypt(params.priceTrigger.type.name, contactKey),
			activePriceValue = eciesEncrypt(params.priceTrigger.value.toString(), contactKey),
			activePriceCurrency = eciesEncrypt(params.priceTrigger.currency, contactKey),
			active = eciesEncrypt(params.active.toString(), contactKey),
			groupUuid = eciesEncrypt(groupUuid ?: NO_GROUP, contactKey),
			currency = eciesEncrypt(params.currency, contactKey),
			commonFriends = commonFriends.map { friend ->
				eciesEncrypt(friend.getHashedContact(), contactKey)
			}
		)
	}

	fun encryptOffer(
		locationHelper: LocationHelper,
		offer: Offer,
		commonFriends: List<BaseContact>,
		contactKey: String,
		offerKeys: KeyPair,
		groupUuid: String?,
	): NewOffer {
		return NewOffer(
			location = offer.location.map {
				eciesEncrypt(locationHelper.locationToJsonString(it), contactKey)
			},
			userPublicKey = contactKey,
			offerPublicKey = eciesEncrypt(offerKeys.publicKey, contactKey),
			feeState = eciesEncrypt(offer.feeState, contactKey),
			feeAmount = eciesEncrypt(offer.feeAmount.toString(), contactKey),
			offerDescription = eciesEncrypt(offer.offerDescription, contactKey),
			amountBottomLimit = eciesEncrypt(offer.amountBottomLimit.toString(), contactKey),
			amountTopLimit = eciesEncrypt(offer.amountTopLimit.toString(), contactKey),
			locationState = eciesEncrypt(offer.locationState, contactKey),
			paymentMethod = offer.paymentMethod.map { eciesEncrypt(it, contactKey) },
			btcNetwork = offer.btcNetwork.map { eciesEncrypt(it, contactKey) },
			friendLevel = eciesEncrypt(offer.friendLevel, contactKey),
			offerType = eciesEncrypt(offer.offerType, contactKey),
			activePriceState = eciesEncrypt(offer.activePriceState, contactKey),
			activePriceValue = eciesEncrypt(offer.activePriceValue.toString(), contactKey),
			activePriceCurrency = eciesEncrypt(offer.activePriceCurrency, contactKey),
			active = eciesEncrypt(offer.active.toString(), contactKey),
			groupUuid = eciesEncrypt(groupUuid ?: NO_GROUP, contactKey),
			currency = eciesEncrypt(offer.currency, contactKey),
			commonFriends = commonFriends.map { friend ->
				eciesEncrypt(friend.getHashedContact(), contactKey)
			}
		)
	}

	private fun eciesEncrypt(data: String, contactKey: String): String =
		EciesCryptoLib.encrypt(contactKey, data)

	@Suppress("ReturnCount")
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
		if (expiration < System.currentTimeMillis()) {
			Toast.makeText(
				activity,
				activity.getString(R.string.error_invalid_offer_delete_trigger),
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
