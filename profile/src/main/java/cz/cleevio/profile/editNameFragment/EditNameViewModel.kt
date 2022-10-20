package cz.cleevio.profile.editNameFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.network.data.ErrorIdentification
import cz.cleevio.network.data.Resource
import cz.cleevio.profile.R
import cz.cleevio.repository.model.user.User
import cz.cleevio.repository.repository.user.UserRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class EditNameViewModel constructor(
	private val userRepository: UserRepository
) : BaseViewModel() {

	private val _wasSuccessful = Channel<Resource<User>>(Channel.CONFLATED)
	val wasSuccessful = _wasSuccessful.receiveAsFlow()

	val user = userRepository.getUserFlow()
	var oldName: String = ""
	var newName: String = ""

	fun editName() {
		viewModelScope.launch(Dispatchers.IO) {
			if (newName.isNotBlank()) {
				userRepository.updateUser(
					username = newName,
				)
				_wasSuccessful.send(Resource.success())

			} else {
				_wasSuccessful.send(
					Resource.error(
						ErrorIdentification.MessageError(message = R.string.error_nickname_blank)
					)
				)
			}
		}
	}
}
