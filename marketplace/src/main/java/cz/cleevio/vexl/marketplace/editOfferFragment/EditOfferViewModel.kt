package cz.cleevio.vexl.marketplace.editOfferFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.model.OfferParams
import cz.cleevio.core.utils.LocationHelper
import cz.cleevio.core.utils.OfferUtils
import cz.cleevio.network.data.ErrorIdentification
import cz.cleevio.network.data.Resource
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import cz.cleevio.vexl.marketplace.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class EditOfferViewModel constructor(
	private val contactRepository: ContactRepository,
	private val offerRepository: OfferRepository,
	private val encryptedPreferenceRepository: EncryptedPreferenceRepository,
	private val locationHelper: LocationHelper
) : BaseViewModel() {

	private val _errorFlow = MutableSharedFlow<Resource<Any>>()
	val errorFlow = _errorFlow.asSharedFlow()

	private val _offer = MutableStateFlow<Offer?>(null)
	val offer = _offer.asStateFlow()

	fun loadMyContactsKeys() {
		viewModelScope.launch(Dispatchers.IO) {
			contactRepository.syncMyContactsKeys()
		}
	}

	fun loadOfferFromCacheById(offerId: String) {
		viewModelScope.launch(Dispatchers.IO) {
			offerRepository.getOffersFlow().collect { offers ->
				_offer.emit(
					offers.firstOrNull { it.offerId == offerId }
				)
			}
		}
	}

	fun updateOffer(offerId: String, params: OfferParams, onSuccess: () -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {

			val offerKeys = offerRepository.loadOfferKeysByOfferId(offerId = offerId)
			if (offerKeys == null) {
				_errorFlow.emit(
					Resource.error(
						ErrorIdentification.MessageError(message = R.string.error_missing_offer_keys)
					)
				)
				return@launch
			}
			val encryptedOfferList = OfferUtils.prepareEncryptedOffers(
				offerKeys = offerKeys,
				params = params,
				contactRepository = contactRepository,
				encryptedPreferenceRepository = encryptedPreferenceRepository,
				locationHelper = locationHelper
			)

			//send all in single request to BE
			val response = offerRepository.updateOffer(
				offerId = offerId,
				encryptedOfferList
			)
			when (response.status) {
				is Status.Success -> {
					withContext(Dispatchers.Main) {
						onSuccess()
					}
				}
				is Status.Error -> {
					_errorFlow.emit(Resource.error(response.errorIdentification))
				}
			}
		}
	}

	fun deleteOffer(offerId: String, onSuccess: () -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			val response = offerRepository.deleteOfferById(offerId)
			when (response.status) {
				is Status.Success -> {
					withContext(Dispatchers.Main) {
						onSuccess()
					}
				}
				is Status.Error -> {
					_errorFlow.emit(Resource.error(response.errorIdentification))
				}
			}
		}
	}
}