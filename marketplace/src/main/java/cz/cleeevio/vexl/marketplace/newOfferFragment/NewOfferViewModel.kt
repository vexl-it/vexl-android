package cz.cleeevio.vexl.marketplace.newOfferFragment

import androidx.lifecycle.viewModelScope
import com.cleevio.vexl.cryptography.KeyPairCryptoLib
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.model.OfferParams
import cz.cleevio.core.utils.LocationHelper
import cz.cleevio.core.utils.OfferUtils
import cz.cleevio.core.widget.FriendLevel
import cz.cleevio.network.data.Resource
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.offer.NewOffer
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel


class NewOfferViewModel constructor(
	private val contactRepository: ContactRepository,
	private val offerRepository: OfferRepository,
	private val encryptedPreferenceRepository: EncryptedPreferenceRepository,
	private val locationHelper: LocationHelper
) : BaseViewModel() {

	private val _newOfferRequest = MutableSharedFlow<Resource<Offer>>()
	val newOfferRequest = _newOfferRequest.asSharedFlow()

	fun createOffer(params: OfferParams) {
		viewModelScope.launch(Dispatchers.IO) {

			_newOfferRequest.emit(Resource.loading())
			val encryptedOfferList: MutableList<NewOffer> = mutableListOf()

			//load all public keys for specified level of friends
			val contacts = when (params.friendLevel.value) {
				FriendLevel.NONE -> emptyList()
				FriendLevel.FIRST_DEGREE -> contactRepository.getFirstLevelContactKeys()
				FriendLevel.SECOND_DEGREE -> contactRepository.getContactKeys()
				else -> emptyList()
			}

			val offerKeys = KeyPairCryptoLib.generateKeyPair()

			//encrypt in loop for every contact
			contacts.forEach { contactKeyWrapper ->
				val encryptedOffer = OfferUtils.encryptOffer(locationHelper, params, contactKeyWrapper.key, offerKeys)
				encryptedOfferList.add(encryptedOffer)
			}

			//also encrypt with user's key
			encryptedPreferenceRepository.userPublicKey.let { myPublicKey ->
				val myEncryptedOffer = OfferUtils.encryptOffer(locationHelper, params, myPublicKey, offerKeys)
				encryptedOfferList.add(myEncryptedOffer)
			}

			//send all in single request to BE
			val response = offerRepository.createOffer(encryptedOfferList)
			when (response.status) {
				is Status.Success -> {
					//save offer ID into DB, also save keys
					response.data?.let { offer ->
						offerRepository.saveMyOfferIdAndKeys(
							offerId = offer.offerId,
							privateKey = offerKeys.privateKey,
							publicKey = offerKeys.publicKey,
							offerType = offer.offerType
						)
					}
					_newOfferRequest.emit(response)
				}
				is Status.Error -> {
					_newOfferRequest.emit(Resource.error(response.errorIdentification))
				}
			}
		}
	}
}