package cz.cleevio.vexl.marketplace.encryptingProgressFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.model.OfferEncryptionData
import cz.cleevio.core.model.OfferParams
import cz.cleevio.core.utils.OfferUtils
import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.model.offer.v2.NewOfferV2
import cz.cleevio.repository.repository.offer.OfferRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class EncryptingProgressViewModel constructor(
	val offerEncryptionData: OfferEncryptionData,
	val encryptedPreferenceRepository: EncryptedPreferenceRepository,
	private val offerRepository: OfferRepository,
	val offerUtils: OfferUtils,
) : BaseViewModel() {

	private val _newOfferRequest = MutableSharedFlow<Resource<Offer>>()
	val newOfferRequest = _newOfferRequest.asSharedFlow()

	private val _encryptedOfferList = MutableSharedFlow<NewOfferV2>()
	val encryptedOfferList = _encryptedOfferList.asSharedFlow()

	fun prepareEncryptedOfferV2() {
		viewModelScope.launch(Dispatchers.IO) {
			offerEncryptionData.run {
				_encryptedOfferList.emit(
					if (offer == null && params != null) {
						offerUtils.prepareEncryptedOfferV2(
							offerKeys = offerKeys,
							params = params as OfferParams,
							locationHelper = locationHelper,
							contactsPublicKeys = contactsPublicKeys,
							commonFriends = commonFriends,
							symmetricalKey = symmetricalKey
						)
					} else {
						offerUtils.prepareEncryptedOfferV2(
							offerKeys = offerKeys,
							offer = offer as Offer,
							locationHelper = locationHelper,
							contactsPublicKeys = contactsPublicKeys,
							commonFriends = commonFriends,
							symmetricalKey = symmetricalKey
						)
					}
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
					expiration = expiration,
					offerKeys = offerKeys,
					offerType = offerType,
					encryptedFor = contactsPublicKeys.map { it.key },
					payloadPublic = offer.payloadPublic,
					symmetricalKey = symmetricalKey,
					friendLevel = friendLevel
				)
				if (response.isSuccess()) {
					contactRepository.refreshUser()
				}
				encryptedPreferenceRepository.isOfferEncrypted = true
				_newOfferRequest.emit(response)
			}
		}
	}

	//send all in single request to BE
	fun sendUpdatedOffer(updateOfferV2: NewOfferV2, offerId: String) {
		viewModelScope.launch(Dispatchers.IO) {
			offerEncryptionData.run {
				val response = offerRepository.updateOffer(
					offerId = offerId,
					offerList = updateOfferV2.privateParts,
					additionalEncryptedFor = contactsPublicKeys.map { it.key },
					payloadPublic = updateOfferV2.payloadPublic
				)
				encryptedPreferenceRepository.isOfferEncrypted = true
				_newOfferRequest.emit(response)
			}
		}
	}
}
