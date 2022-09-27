package cz.cleevio.vexl.contacts.notificationFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.repository.repository.user.UserRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class NotificationViewModel constructor(
	private val userRepository: UserRepository,
	val navMainGraphModel: NavMainGraphModel
) : BaseViewModel() {

	private val hasPermissionsChannel = Channel<Boolean>(Channel.CONFLATED)
	val hasPermissionsEvent = hasPermissionsChannel.receiveAsFlow()

	fun updateHasPostNotificationsPermissions(hasPermissions: Boolean) {
		hasPermissionsChannel.trySend(hasPermissions)
	}

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