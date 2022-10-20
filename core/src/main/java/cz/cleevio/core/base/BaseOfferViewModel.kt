package cz.cleevio.core.base

import androidx.lifecycle.viewModelScope
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.model.OfferEncryptionData
import cz.cleevio.core.model.OfferParams
import cz.cleevio.core.utils.OfferUtils
import cz.cleevio.core.widget.OfferLocationItem
import cz.cleevio.repository.model.contact.BaseContact
import cz.cleevio.repository.model.contact.ContactKey
import cz.cleevio.repository.model.offer.LocationSuggestion
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.group.GroupRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import cz.cleevio.repository.repository.user.UserRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class BaseOfferViewModel constructor(
	private val userRepository: UserRepository,
	private val contactRepository: ContactRepository,
	private val offerRepository: OfferRepository,
	private val groupRepository: GroupRepository,
	private val encryptedPreferenceRepository: EncryptedPreferenceRepository,
	private val offerUtils: OfferUtils
) : BaseViewModel() {

	val userFlow = userRepository.getUserFlow()

	private val _contactsPublicKeys = MutableSharedFlow<Pair<OfferParams, List<ContactKey>>>()
	val contactsPublicKeys = _contactsPublicKeys.asSharedFlow()

	private val _commonFriends = MutableSharedFlow<Map<String, List<BaseContact>>>()
	val commonFriends = _commonFriends.asSharedFlow()

	protected val showEncryptingDialog = MutableSharedFlow<OfferEncryptionData>()
	val showEncryptingDialogFlow = showEncryptingDialog.asSharedFlow()

	val groups = groupRepository.getGroupsFlow()

	private val _queryForSuggestions = MutableStateFlow<Pair<OfferLocationItem?, String>>(Pair(null, ""))

	@OptIn(FlowPreview::class)
	val queryForSuggestions = _queryForSuggestions.asStateFlow()
		.debounce(DEBOUNCE)

	private val _suggestions: MutableStateFlow<Pair<OfferLocationItem?, List<LocationSuggestion>>> =
		MutableStateFlow(Pair(null, listOf()))
	val suggestions = _suggestions.asStateFlow()

	val contactKeysAndCommonFriends = _contactsPublicKeys.combine(
		_commonFriends
	) { (params, contactKeys), commonFriends ->
		Triple(params, contactKeys, commonFriends)
	}

	fun loadMyContactsKeys() {
		viewModelScope.launch(Dispatchers.IO) {
			contactRepository.syncMyContactsKeys()
			groupRepository.syncAllGroupsMembers()
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

	fun fetchContactsPublicKeys(params: OfferParams) {
		viewModelScope.launch(Dispatchers.IO) {
			_contactsPublicKeys.emit(
				Pair(
					params,
					offerUtils.fetchContactsPublicKeysV2(
						friendLevel = params.friendLevel.value,
						groupUuids = params.groupUuids,
						contactRepository = contactRepository,
						encryptedPreferenceRepository = encryptedPreferenceRepository,
						shouldEmitContacts = true
					)
				)
			)
		}
	}

	fun fetchCommonFriends(contactsPublicKeys: List<ContactKey>) {
		viewModelScope.launch(Dispatchers.IO) {
			_commonFriends.emit(
				contactRepository.getCommonFriends(
					contactsPublicKeys
						.distinctBy { it.key }
						.map { it.key }
						.filter {
							// we don't want to get common friends for our offer
							it != encryptedPreferenceRepository.userPublicKey
						}
				))
		}
	}

	private companion object {
		private const val DEBOUNCE = 300L
		private const val SUGGESTION_COUNT = 20
		private const val SUGGESTION_LANGUAGES = "cs,en,sk"
	}
}
