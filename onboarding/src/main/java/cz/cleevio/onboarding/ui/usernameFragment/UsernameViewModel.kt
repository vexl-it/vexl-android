package cz.cleevio.onboarding.ui.usernameFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.repository.model.user.UsernameAvailable
import cz.cleevio.repository.repository.user.UserRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class UsernameViewModel constructor(
	private val userRepository: UserRepository
) : BaseViewModel() {

	private val _loading = Channel<Boolean>()
	val loading = _loading.receiveAsFlow()

	private val _usernameAvailable = Channel<UsernameAvailable>()
	val usernameAvailable = _usernameAvailable.receiveAsFlow()

	fun checkUsernameAvailability(username: String) {
		viewModelScope.launch(Dispatchers.IO) {
//			_loading.send(true)
//			val response = userRepository.isUsernameAvailable(username)
//			_loading.send(false)
//			response.data?.let {
			_usernameAvailable.send(UsernameAvailable(true, username))
//			}
		}
	}
}
