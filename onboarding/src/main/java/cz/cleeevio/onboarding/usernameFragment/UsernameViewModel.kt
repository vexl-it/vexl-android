package cz.cleeevio.onboarding.usernameFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.repository.model.user.UsernameAvailable
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class UsernameViewModel constructor(
	private val userRepository: UserRepository
) : BaseViewModel() {

	private val _usernameAvailable = MutableSharedFlow<UsernameAvailable>(replay = 1)
	val usernameAvailable = _usernameAvailable.asSharedFlow()

	fun checkUsernameAvailability(username: String) {
		viewModelScope.launch(Dispatchers.IO) {
			val response = userRepository.isUsernameAvailable(username)
			response.data?.let {
				_usernameAvailable.emit(it)
			}
		}
	}
}