package cz.cleevio.vexl.contacts.contactsListFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.base.BaseContactsListViewModel
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactsListViewModel constructor(
	private val contactRepository: ContactRepository,
	private val userRepository: UserRepository,
	navMainGraphModel: NavMainGraphModel,
	encryptedPreferenceRepository: EncryptedPreferenceRepository
) : BaseContactsListViewModel(contactRepository, navMainGraphModel, encryptedPreferenceRepository) {

	fun finishOnboardingAndNavigateToMain() {
		viewModelScope.launch(Dispatchers.Default) {
			userRepository.getUser()?.let { user ->
				userRepository.markUserFinishedOnboarding(user)
			}

			navMainGraphModel.navigateToGraph(
				NavMainGraphModel.NavGraph.Main
			)
		}
	}
}