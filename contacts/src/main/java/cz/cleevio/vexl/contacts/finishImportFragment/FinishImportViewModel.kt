package cz.cleevio.vexl.contacts.finishImportFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.user.UserRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class FinishImportViewModel constructor(
	private val userRepository: UserRepository,
	private val contactRepository: ContactRepository,
	val navMainGraphModel: NavMainGraphModel
) : BaseViewModel() {

	private val _sshKeyDownloadSuccessful = MutableSharedFlow<Boolean>(replay = 1)
	val sshKeyDownloadSuccessful = _sshKeyDownloadSuccessful.asSharedFlow()

	init {
		viewModelScope.launch {
			userRepository.getUser()?.let { user ->
				userRepository.markUserFinishedOnboarding(user)
			}
		}
	}

	fun loadMyContactsKeys() {
		viewModelScope.launch(Dispatchers.IO) {
			val success = contactRepository.syncMyContactsKeys()
			_sshKeyDownloadSuccessful.emit(success)
		}
	}
}