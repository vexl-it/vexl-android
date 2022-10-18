package cz.cleevio.vexl.marketplace.editOfferFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.base.BaseOfferViewModel
import cz.cleevio.core.model.OfferEncryptionData
import cz.cleevio.core.model.OfferParams
import cz.cleevio.core.utils.LocationHelper
import cz.cleevio.core.utils.OfferUtils
import cz.cleevio.network.data.ErrorIdentification
import cz.cleevio.network.data.Resource
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.contact.BaseContact
import cz.cleevio.repository.model.contact.ContactKey
import cz.cleevio.repository.model.offer.MyOffer
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.group.GroupRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import cz.cleevio.repository.repository.user.UserRepository
import cz.cleevio.vexl.marketplace.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class EditOfferViewModel constructor(
	private val userRepository: UserRepository,
	private val contactRepository: ContactRepository,
	private val offerRepository: OfferRepository,
	private val groupRepository: GroupRepository,
	val encryptedPreferenceRepository: EncryptedPreferenceRepository,
	private val locationHelper: LocationHelper,
	val offerUtils: OfferUtils,
) : BaseOfferViewModel(
	userRepository,
	contactRepository,
	offerRepository,
	groupRepository,
	encryptedPreferenceRepository,
	offerUtils
) {

	private val _errorFlow = MutableSharedFlow<Resource<Any>>()
	val errorFlow = _errorFlow.asSharedFlow()

	private val _offer = MutableStateFlow<Offer?>(null)
	val offer = _offer.asStateFlow()

	private val _myOffer = MutableStateFlow<MyOffer?>(null)
	val myOffer = _myOffer.asStateFlow()

	val offerAndMyOffer = _offer.combine(
		_myOffer
	) { offer, myOffer ->
		Pair(offer, myOffer)
	}

	fun loadOfferFromCacheById(offerId: String) {
		viewModelScope.launch(Dispatchers.IO) {
			offerRepository.getOffersFlow().collect { offers ->
				_offer.emit(
					offers.firstOrNull { it.offerId == offerId }
				)
			}
		}

		viewModelScope.launch(Dispatchers.IO) {
			_myOffer.emit(
				offerRepository.getMyOffers().firstOrNull {
					it.offerId == offerId
				}
			)
		}
	}

	fun updateOffer(offerId: String, params: OfferParams, contactKeys: List<ContactKey>, commonFriends: Map<String, List<BaseContact>>) {
		viewModelScope.launch(Dispatchers.IO) {

			val offerKeys = offerRepository.loadOfferKeysByOfferId(offerId = offerId)
			val symmetricalKey = offerRepository.loadSymmetricalKeyByOfferId(offerId = offerId)
			if (offerKeys == null || symmetricalKey == null) {
				_errorFlow.emit(
					Resource.error(
						ErrorIdentification.MessageError(message = R.string.error_missing_offer_keys)
					)
				)
				return@launch
			}

			showEncryptingDialog.emit(
				OfferEncryptionData(
					offerKeys = offerKeys,
					params = params,
					contactRepository = contactRepository,
					encryptedPreferenceRepository = encryptedPreferenceRepository,
					locationHelper = locationHelper,
					offerId = offerId,
					contactsPublicKeys = contactKeys,
					commonFriends = commonFriends,
					symmetricalKey = symmetricalKey
				)
			)
		}
	}

	fun deleteOffer(offerId: String, onSuccess: () -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			val response = offerRepository.deleteMyOfferById(offerId)
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
