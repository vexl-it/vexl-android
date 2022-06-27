package cz.cleeevio.vexl.marketplace.editOfferFragment

import androidx.lifecycle.viewModelScope
import cz.cleeevio.vexl.marketplace.R
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.model.OfferParams
import cz.cleevio.core.utils.LocationHelper
import cz.cleevio.core.utils.OfferUtils
import cz.cleevio.core.widget.FriendLevel
import cz.cleevio.network.data.ErrorIdentification
import cz.cleevio.network.data.Resource
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.offer.NewOffer
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lightbase.core.baseClasses.BaseViewModel


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

			val encryptedOfferList: MutableList<NewOffer> = mutableListOf()
			val offerKeys = offerRepository.loadOfferKeysByOfferId(offerId = offerId)
			if (offerKeys == null) {
				_errorFlow.emit(
					Resource.error(
						ErrorIdentification.MessageError(message = R.string.error_missing_offer_keys)
					)
				)
				return@launch
			}

			//load all public keys for specified level of friends
			val contactsPublicKeys = when (params.friendLevel.value) {
				FriendLevel.NONE -> emptyList()
				FriendLevel.FIRST_DEGREE -> contactRepository.getFirstLevelContactKeys()
				FriendLevel.SECOND_DEGREE -> contactRepository.getContactKeys()
				else -> emptyList()
			}.map {
				it.key // get just the keys
			}.toMutableSet() // remove duplicities

			//also add user's key
			encryptedPreferenceRepository.userPublicKey.let { myPublicKey ->
				contactsPublicKeys.add(myPublicKey)
			}

			contactsPublicKeys.forEach { key ->
				val encryptedOffer = OfferUtils.encryptOffer(locationHelper, params, key, offerKeys)
				encryptedOfferList.add(encryptedOffer)
			}

			//send all in single request to BE
			val response = offerRepository.updateOffer(
				offerId = offerId,
				encryptedOfferList
			)
			when (response.status) {
				is Status.Success -> {
					val updateResponse = offerRepository.refreshOffer(offerId)
					when (updateResponse.status) {
						is Status.Success -> {
							withContext(Dispatchers.Main) {
								onSuccess()
							}
						}
						is Status.Error -> {
							_errorFlow.emit(Resource.error(updateResponse.errorIdentification))
						}
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