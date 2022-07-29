package cz.cleevio.vexl.marketplace.newOfferFragment

import androidx.lifecycle.viewModelScope
import com.cleevio.vexl.cryptography.KeyPairCryptoLib
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.model.OfferParams
import cz.cleevio.core.utils.LocationHelper
import cz.cleevio.core.utils.OfferUtils
import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.repository.chat.ChatRepository
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch


class NewOfferViewModel constructor(
	private val contactRepository: ContactRepository,
	private val offerRepository: OfferRepository,
	private val chatRepository: ChatRepository,
	private val encryptedPreferenceRepository: EncryptedPreferenceRepository,
	private val locationHelper: LocationHelper
) : BaseViewModel() {

	private val _newOfferRequest = MutableSharedFlow<Resource<Offer>>()
	val newOfferRequest = _newOfferRequest.asSharedFlow()

	fun loadMyContactsKeys() {
		viewModelScope.launch(Dispatchers.IO) {
			contactRepository.syncMyContactsKeys()
		}
	}

	fun createOffer(params: OfferParams) {
		viewModelScope.launch(Dispatchers.IO) {

			_newOfferRequest.emit(Resource.loading())
			val offerKeys = KeyPairCryptoLib.generateKeyPair()
			val encryptedOfferList = OfferUtils.prepareEncryptedOffers(
				offerKeys = offerKeys,
				params = params,
				contactRepository = contactRepository,
				encryptedPreferenceRepository = encryptedPreferenceRepository,
				locationHelper = locationHelper
			)

			//send all in single request to BE
			val response = offerRepository.createOffer(encryptedOfferList, params.expiration, offerKeys)
			_newOfferRequest.emit(response)
		}
	}
}