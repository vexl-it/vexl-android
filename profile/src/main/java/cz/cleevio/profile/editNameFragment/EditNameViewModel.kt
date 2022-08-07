package cz.cleevio.profile.editNameFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.repository.repository.user.UserRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class EditNameViewModel constructor(
	private val userRepository: UserRepository
) : BaseViewModel() {

	private val _wasSuccessful = Channel<Boolean>(Channel.CONFLATED)
	val wasSuccessful = _wasSuccessful.receiveAsFlow()

	val oldName = userRepository.getUserFlow()

	var newName: String = ""

	fun editName() {
		viewModelScope.launch(Dispatchers.IO) {
			if (newName.isNotBlank()) {
				userRepository.updateUser(
					username = newName,
					avatar = null
				).let {
					_wasSuccessful.send(it.isSuccess())
				}
			} else {
				_wasSuccessful.send(false)
			}
		}
	}
}
