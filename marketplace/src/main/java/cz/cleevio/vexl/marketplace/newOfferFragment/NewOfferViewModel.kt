package cz.cleevio.vexl.marketplace.newOfferFragment

import androidx.lifecycle.viewModelScope
import com.cleevio.vexl.cryptography.KeyPairCryptoLib
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.model.OfferParams
import cz.cleevio.core.utils.LocationHelper
import cz.cleevio.core.utils.OfferUtils
import cz.cleevio.core.widget.OfferLocationItem
import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.offer.LocationSuggestion
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.repository.chat.ChatRepository
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import cz.cleevio.repository.repository.user.UserRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NewOfferViewModel constructor(
	private val userRepository: UserRepository,
	private val contactRepository: ContactRepository,
	private val offerRepository: OfferRepository,
	private val chatRepository: ChatRepository,
	private val encryptedPreferenceRepository: EncryptedPreferenceRepository,
	private val locationHelper: LocationHelper
) : BaseViewModel() {

	val userFlow = userRepository.getUserFlow()

	private val _newOfferRequest = MutableSharedFlow<Resource<Offer>>()
	val newOfferRequest = _newOfferRequest.asSharedFlow()

	private val _queryForSuggestions = MutableStateFlow<Pair<OfferLocationItem?, String>>(Pair(null, ""))

	@OptIn(FlowPreview::class)
	val queryForSuggestions = _queryForSuggestions.asStateFlow()
		.debounce(DEBOUNCE)

	private val _suggestions: MutableStateFlow<Pair<OfferLocationItem?, List<LocationSuggestion>>> =
		MutableStateFlow(Pair(null, listOf()))
	val suggestions = _suggestions.asStateFlow()

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

	fun getSuggestions(query: String, viewReference: OfferLocationItem) {
		viewModelScope.launch(Dispatchers.IO) {
			_queryForSuggestions.emit(Pair(viewReference, query))
		}
	}

	fun getDebouncedSuggestions(query: String, viewReference: OfferLocationItem) {
		viewModelScope.launch(Dispatchers.IO) {
			val result = offerRepository.getLocationSuggestions(SUGGESTION_COUNT, query, SUGGESTION_LANGUAGES)
			if (result.isSuccess()) {
				_suggestions.emit(Pair(viewReference, result.data.orEmpty()))
			}
		}
	}

	private companion object {
		private const val DEBOUNCE = 300L
		private const val SUGGESTION_COUNT = 20
		private const val SUGGESTION_LANGUAGES = "cz,en,sk"
	}
}
