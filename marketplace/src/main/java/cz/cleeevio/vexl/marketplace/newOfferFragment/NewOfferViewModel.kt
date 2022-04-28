package cz.cleeevio.vexl.marketplace.newOfferFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.utils.LocationHelper
import cz.cleevio.core.widget.FriendLevel
import com.cleevio.vexl.cryptography.EciesCryptoLib
import com.cleevio.vexl.cryptography.KeyPairCryptoLib
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.utils.LocationHelper
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.offer.NewOffer
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel


class NewOfferViewModel constructor(
	private val contactRepository: ContactRepository,
	private val offerRepository: OfferRepository,
	private val encryptedPreferenceRepository: EncryptedPreferenceRepository,
	private val locationHelper: LocationHelper
) : BaseViewModel() {

	fun createOffer(params: NewOfferParams) {
		viewModelScope.launch(Dispatchers.IO) {
			val encryptedOfferList: MutableList<NewOffer> = mutableListOf()

			//load all public keys for specified level of friends
			val contacts = when (params.friendLevel.value) {
				FriendLevel.NONE -> emptyList()
				FriendLevel.FIRST_DEGREE -> contactRepository.getFirstLevelContactKeys()
				FriendLevel.SECOND_DEGREE -> contactRepository.getContactKeys()
				else -> emptyList()
			}

			//encrypt in loop for every contact
			contacts.forEach { contactKeyWrapper ->

				// TODO we have to save them both
				val offerKeys = KeyPairCryptoLib.generateKeyPair()

				val contactKey = contactKeyWrapper.key


				val encryptedOffer = NewOffer(
					location = params.location.values.map {
						// FIXME proper serialization
						it.toString()
					},
					userPublicKey = contactKey,
					offerPublicKey = eciesEncrypt(offerKeys.publicKey, contactKey),
					feeState = eciesEncrypt(params.fee.type.toString(), contactKey),                        // FIXME
					feeAmount = eciesEncrypt(params.fee.value.toString(), contactKey),
					offerDescription = eciesEncrypt(params.description, contactKey),
					amountBottomLimit = eciesEncrypt(params.priceRange.bottomLimit.toString(), contactKey),
					amountTopLimit = eciesEncrypt(params.priceRange.topLimit.toString(), contactKey),
					locationState = eciesEncrypt(params.location.values.toString(), contactKey),            // FIXME
					paymentMethod = params.paymentMethod.value.map { eciesEncrypt(it.toString(), contactKey) },                // FIXME
					btcNetwork = params.btcNetwork.value.map { eciesEncrypt(it.toString(), contactKey) },            // FIXME
					friendLevel = params.friendLevel.value.toString()            // FIXME
				)
				encryptedOfferList.add(encryptedOffer)
			}

			//also encrypt with user's key
			encryptedPreferenceRepository.userPublicKey.let { publicKey ->
				//TODO: encrypt all data fields with public key
				val encryptedOffer = NewOffer(
					location = params.location.values.map { location -> locationHelper.locationToJsonString(location) }
				)
				encryptedOfferList.add(encryptedOffer)
			}

			//send all in single request to BE
			val response = offerRepository.createOffer(encryptedOfferList)
			when (response.status) {
				is Status.Success -> {
					//save offer ID into DB, also save keys?
					response.data?.offerId?.let { offerId ->
						offerRepository.saveMyOfferIdAndKeys(
							offerId = offerId,
							//todo: add keys that were generated for offer
							privateKey = "",
							publicKey = ""
						)
					}

					//TODO: notify UI and move user to some other screen?
				}
			}
		}
	}

	private fun eciesEncrypt(data: String, contactKey: String): String {
		return EciesCryptoLib.encrypt(contactKey, data)
	}

}