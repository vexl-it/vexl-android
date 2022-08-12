package cz.cleevio.core.utils

import android.app.Activity
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
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
import cz.cleevio.repository.repository.contact.ContactRepository

object OfferUtils {

	const val NO_GROUP = "NONE"

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
			FriendLevel.FIRST_DEGREE -> contactRepository.getFirstLevelContactKeys() + contactRepository.getGroupsContactKeys()
			FriendLevel.SECOND_DEGREE -> contactRepository.getContactKeys()
			else -> emptyList()
		}
			.toMutableList()
			.also {
				//also add user's key
				encryptedPreferenceRepository.userPublicKey.let { myPublicKey ->
					it.add(
						ContactKey(
							key = myPublicKey,
							level = ContactLevel.NOT_SPECIFIED,
							groupUuid = null
						)
					)
				}
			}.distinctBy {
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
		contactsPublicKeys.forEach { contactKey ->
			val encryptedOffer = encryptOffer(
				locationHelper = locationHelper,
				params = params,
				// TODO orEmpty should not happen, list in map is not nullable
				commonFriends = commonFriends[contactKey.key].orEmpty(),
				contactKey = contactKey.key,
				offerKeys = offerKeys,
				groupUuid = contactKey.groupUuid
			)
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
			active = eciesEncrypt(params.active.toString(), contactKey),
			groupUuid = eciesEncrypt(groupUuid ?: NO_GROUP, contactKey),
			currency = eciesEncrypt(params.currency, contactKey),
			commonFriends = commonFriends.map { friend ->
				eciesEncrypt(friend.getHashedContact(), contactKey)
			}
		)
	}

	private fun eciesEncrypt(data: String, contactKey: String): String =
		EciesCryptoLib.encrypt(contactKey, data)

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
		currency: String = "CZK"
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
			currency = currency
		)
	}
}
