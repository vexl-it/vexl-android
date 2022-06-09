package cz.cleeevio.vexl.marketplace.editOfferFragment

import androidx.lifecycle.viewModelScope
import com.cleevio.vexl.cryptography.EciesCryptoLib
import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleeevio.vexl.marketplace.R
import cz.cleeevio.vexl.marketplace.newOfferFragment.NewOfferParams
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.utils.LocationHelper
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
import lightbase.core.baseClasses.BaseViewModel


class EditOfferViewModel constructor(
	private val contactRepository: ContactRepository,
	private val offerRepository: OfferRepository,
	private val encryptedPreferenceRepository: EncryptedPreferenceRepository,
	private val locationHelper: LocationHelper
) : BaseViewModel() {

	private val _updateOfferRequest = MutableSharedFlow<Resource<Offer>>()
	val updateOfferRequest = _updateOfferRequest.asSharedFlow()

	private val _offer = MutableStateFlow<Offer?>(null)
	val offer = _offer.asStateFlow()

	fun loadOfferById(offerId: String) {
		viewModelScope.launch(Dispatchers.IO) {
			offerRepository.getOffersFlow().collect { offers ->
				_offer.emit(
					offers.firstOrNull { it.offerId == offerId }
				)
			}
		}
	}

	fun updateOffer(offerId: String, params: NewOfferParams) {
		viewModelScope.launch(Dispatchers.IO) {

			_updateOfferRequest.emit(Resource.loading())
			val encryptedOfferList: MutableList<NewOffer> = mutableListOf()

			//load all public keys for specified level of friends
			val contacts = when (params.friendLevel.value) {
				FriendLevel.NONE -> emptyList()
				FriendLevel.FIRST_DEGREE -> contactRepository.getFirstLevelContactKeys()
				FriendLevel.SECOND_DEGREE -> contactRepository.getContactKeys()
				else -> emptyList()
			}

			val offerKeys = offerRepository.loadOfferKeysByOfferId(offerId = offerId)
			if (offerKeys == null) {
				_updateOfferRequest.emit(
					Resource.error(
						ErrorIdentification.MessageError(message = R.string.error_missing_offer_keys)
					)
				)
				return@launch
			}

			//encrypt in loop for every contact
			contacts.forEach { contactKeyWrapper ->
				val encryptedOffer = encryptOffer(params, contactKeyWrapper.key, offerKeys)
				encryptedOfferList.add(encryptedOffer)
			}

			//also encrypt with user's key
			encryptedPreferenceRepository.userPublicKey.let { myPublicKey ->
				val myEncryptedOffer = encryptOffer(params, myPublicKey, offerKeys)
				encryptedOfferList.add(myEncryptedOffer)
			}

			//send all in single request to BE
			val response = offerRepository.updateOffer(
				offerId = offerId,
				encryptedOfferList
			)
			when (response.status) {
				is Status.Success -> {
					_updateOfferRequest.emit(response)
				}
				is Status.Error -> {
					_updateOfferRequest.emit(Resource.error(response.errorIdentification))
				}
			}
		}
	}

	private fun encryptOffer(params: NewOfferParams, contactKey: String, offerKeys: KeyPair): NewOffer {
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
			offerType = eciesEncrypt(params.offerType, contactKey)
		)
	}

	private fun eciesEncrypt(data: String, contactKey: String): String {
		return EciesCryptoLib.encrypt(contactKey, data)
	}
}