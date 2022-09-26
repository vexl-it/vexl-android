package cz.cleevio.vexl.marketplace.newOfferFragment

import androidx.lifecycle.viewModelScope
import com.cleevio.vexl.cryptography.KeyPairCryptoLib
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.model.OfferEncryptionData
import cz.cleevio.core.model.OfferParams
import cz.cleevio.core.utils.LocationHelper
import cz.cleevio.core.utils.OfferUtils
import cz.cleevio.core.widget.OfferLocationItem
import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.offer.LocationSuggestion
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

class NewOfferViewModel constructor(
	private val userRepository: UserRepository,
	private val contactRepository: ContactRepository,
	private val offerRepository: OfferRepository,
	private val groupRepository: GroupRepository,
	private val encryptedPreferenceRepository: EncryptedPreferenceRepository,
	private val locationHelper: LocationHelper
) : BaseViewModel() {

	val userFlow = userRepository.getUserFlow()
	var isTriggerSectionShowed = true
	var isAdvancedSectionShowed = true

	private val _showEncryptingDialog = MutableSharedFlow<OfferEncryptionData>()
	val showEncryptingDialog = _showEncryptingDialog.asSharedFlow()

	val groups = groupRepository.getGroupsFlow()

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
			groupRepository.syncAllGroupsMembers()
		}
	}

	fun createOffer(params: OfferParams) {
		viewModelScope.launch(Dispatchers.IO) {
			val offerKeys = KeyPairCryptoLib.generateKeyPair()

			_showEncryptingDialog.emit(
				OfferEncryptionData(
					offerKeys = offerKeys,
					params = params,
					contactRepository = contactRepository,
					encryptedPreferenceRepository = encryptedPreferenceRepository,
					locationHelper = locationHelper
				)
			)
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
		private const val SUGGESTION_LANGUAGES = "cs,en,sk"
	}
}
