package cz.cleevio.vexl.marketplace.encryptingProgressFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.core.model.OfferEncryptionData
import cz.cleevio.core.utils.OfferUtils
import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.offer.NewOffer
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.repository.offer.OfferRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class EncryptingProgressViewModel constructor(
	val offerEncryptionData: OfferEncryptionData,
	private val offerRepository: OfferRepository
) : BaseViewModel() {

	private val _newOfferRequest = MutableSharedFlow<Resource<Offer>>()
	val newOfferRequest = _newOfferRequest.asSharedFlow()

	private val _encryptedOfferList = MutableSharedFlow<List<NewOffer>>()
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
	fun sendNewOffer(encryptedOfferList: List<NewOffer>) {
		viewModelScope.launch(Dispatchers.IO) {
			offerEncryptionData.run {
				val response = offerRepository.createOffer(
					offerList = encryptedOfferList,
					expiration = params.expiration,
					offerKeys = offerKeys,
					offerType = params.offerType,
					encryptedFor = contactsPublicKeys.map { it.key }
				)

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
				_newOfferRequest.emit(response)
			}
		}
	}
}
