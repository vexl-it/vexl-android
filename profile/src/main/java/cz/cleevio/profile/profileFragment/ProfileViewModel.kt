package cz.cleevio.profile.profileFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.model.Currency
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.network.data.Status
import cz.cleevio.repository.repository.chat.ChatRepository
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import cz.cleevio.repository.repository.user.UserRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProfileViewModel constructor(
	private val userRepository: UserRepository,
	private val contactRepository: ContactRepository,
	private val offerRepository: OfferRepository,
	private val chatRepository: ChatRepository,
	val encryptedPreferenceRepository: EncryptedPreferenceRepository,
	val navMainGraphModel: NavMainGraphModel
) : BaseViewModel() {

	val userFlow = userRepository.getUserFlow()

	val areScreenshotsAllowed
		get() = encryptedPreferenceRepository.areScreenshotsAllowed

	private val _contactsNumber = MutableStateFlow<Int>(0)
	val contactsNumber = _contactsNumber.asStateFlow()

	private val hasPermissionsChannel = Channel<Boolean>(Channel.CONFLATED)
	val hasPermissionsEvent = hasPermissionsChannel.receiveAsFlow()

	fun updateHasReadContactPermissions(hasPermissions: Boolean) {
		hasPermissionsChannel.trySend(hasPermissions)
	}

	fun logout(onSuccess: () -> Unit = {}, onError: () -> Unit = {}) {
		viewModelScope.launch(Dispatchers.IO) {
			// TODO delete all offers that are connected to my person - please rather check
			val offerDelete = offerRepository.deleteMyOffers(offerRepository.getMyOffersWithoutInbox().map { it.offerId })
			val userDelete = userRepository.deleteMe()
			val contactUserDelete = contactRepository.deleteMyUser()
			val contactFacebookDelete = contactRepository.deleteMyFacebookUser()

			// TODO add removing for GroupDao if needed
			// Delete also entities stored in the local database
			userRepository.deleteLocalUser()
			offerRepository.clearOfferTables()
			chatRepository.clearChatTables()
			encryptedPreferenceRepository.clearPreferences()

			//todo: delete also all offers, when we have system for keeping offer IDs
			if (userDelete.status is Status.Success &&
				contactUserDelete.status is Status.Success &&
				contactFacebookDelete.status is Status.Success &&
				offerDelete.status is Status.Success
			) {
				withContext(Dispatchers.Main) {
					onSuccess()
				}
			} else {
				withContext(Dispatchers.Main) {
					onError()
				}
			}
		}
	}

	fun navigateToOnboarding() {
		viewModelScope.launch(Dispatchers.Default) {
			navMainGraphModel.navigateToGraph(
				NavMainGraphModel.NavGraph.Onboarding
			)
		}
	}

	fun updateAllowScreenshotsSettings() {
		encryptedPreferenceRepository.areScreenshotsAllowed = !encryptedPreferenceRepository.areScreenshotsAllowed
	}

	fun setCurrency(currency: Currency) {
		encryptedPreferenceRepository.selectedCurrency = currency.name
	}

}
