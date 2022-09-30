package cz.cleevio.vexl.contacts.facebookContactsListFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.core.base.BaseFacebookContactsListViewModel
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FacebookContactsListViewModel constructor(
	private val contactRepository: ContactRepository,
	private val userRepository: UserRepository,
	navMainGraphModel: NavMainGraphModel
) : BaseFacebookContactsListViewModel(contactRepository, navMainGraphModel) {

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