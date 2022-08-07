package cz.cleevio.core.utils

import com.cleevio.vexl.cryptography.EciesCryptoLib
import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.model.OfferParams
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
			val encryptedOffer = OfferUtils.encryptOffer(
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
}
