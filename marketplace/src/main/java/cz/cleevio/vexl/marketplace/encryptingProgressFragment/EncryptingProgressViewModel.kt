package cz.cleevio.vexl.marketplace.encryptingProgressFragment

import androidx.lifecycle.viewModelScope
import com.cleevio.vexl.cryptography.KeyPairCryptoLib
import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.model.OfferEncryptionData
import cz.cleevio.core.model.OfferParams
import cz.cleevio.core.utils.LocationHelper
import cz.cleevio.core.utils.OfferUtils
import cz.cleevio.core.widget.OfferLocationItem
import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.offer.LocationSuggestion
import cz.cleevio.repository.model.offer.NewOffer
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.group.GroupRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import cz.cleevio.repository.repository.user.UserRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
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
						contactRepository = contactRepository,
						encryptedPreferenceRepository = encryptedPreferenceRepository,
						locationHelper = locationHelper
					)
				)
			}
		}
	}

	fun sendRequest(encryptedOfferList: List<NewOffer>) {
		viewModelScope.launch(Dispatchers.IO) {
			offerEncryptionData.run {
				//send all in single request to BE
				val response = offerRepository.createOffer(encryptedOfferList, params.expiration, offerKeys, offerType = params.offerType)
				_newOfferRequest.emit(response)
			}
		}
	}
}
