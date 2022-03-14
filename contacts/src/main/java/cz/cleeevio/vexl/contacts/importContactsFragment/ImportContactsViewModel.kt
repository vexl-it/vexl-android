package cz.cleeevio.vexl.contacts.importContactsFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.cache.entity.UserEntity
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class ImportContactsViewModel constructor(
	private val userRepository: UserRepository
) : BaseViewModel() {

	private val _user = MutableSharedFlow<UserEntity?>(replay = 1)
	val user = _user.asSharedFlow()

	fun getUserData() {
		viewModelScope.launch(Dispatchers.IO) {
			_user.emit(userRepository.getUser())
		}
	}
}