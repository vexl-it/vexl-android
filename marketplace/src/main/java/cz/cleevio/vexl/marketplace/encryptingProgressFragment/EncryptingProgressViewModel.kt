package cz.cleevio.vexl.marketplace.encryptingProgressFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.model.OfferEncryptionData
import cz.cleevio.core.utils.OfferUtils
import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.offer.NewOffer
import cz.cleevio.repository.model.offer.NewOfferV2
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.repository.offer.OfferRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class EncryptingProgressViewModel constructor(
	val offerEncryptionData: OfferEncryptionData,
	val encryptedPreferenceRepository: EncryptedPreferenceRepository,
	private val offerRepository: OfferRepository
) : BaseViewModel() {

	private val _newOfferRequest = MutableSharedFlow<Resource<Offer>>()
	val newOfferRequest = _newOfferRequest.asSharedFlow()

	private val _encryptedOfferList = MutableSharedFlow<List<NewOfferV2>>()
	val encryptedOfferList = _encryptedOfferList.asSharedFlow()

	fun prepareEncryptedOffers() {
		viewModelScope.launch(Dispatchers.IO) {
			offerEncryptionData.run {
				_encryptedOfferList.emit(
					OfferUtils.prepareEncryptedOffers(
						offerKeys = offerKeys,
						params = params,
						locationHelper = locationHelper,
						contactsPublicKeys = contactsPublicKeys,
						commonFriends = commonFriends
					)
				)
			}
		}
	}

	//send all in single request to BE
	fun sendNewOffer(offer: NewOfferV2) {
		viewModelScope.launch(Dispatchers.IO) {
			offerEncryptionData.run {
				val response = offerRepository.createOffer(
					offerList = offer.privateParts,
					expiration = params.expiration,
					offerKeys = offerKeys,
					offerType = params.offerType,
					encryptedFor = contactsPublicKeys.map { it.key },
					payloadPublic = offer.payloadPublic
				)
				encryptedPreferenceRepository.isOfferEncrypted = true
				_newOfferRequest.emit(response)
			}
		}
	}

	//send all in single request to BE
	fun sendUpdatedOffer(encryptedOfferList: List<NewOffer>, offerId: String) {
		viewModelScope.launch(Dispatchers.IO) {
			offerEncryptionData.run {
				val response = offerRepository.updateOffer(
					offerId = offerId,
					offerList = encryptedOfferList,
					additionalEncryptedFor = contactsPublicKeys.map { it.key }
				)
				encryptedPreferenceRepository.isOfferEncrypted = true
				_newOfferRequest.emit(response)
			}
		}
	}
}
