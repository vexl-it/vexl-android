package cz.cleeevio.vexl.contacts.finishImportFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class FinishImportViewModel constructor(
	private val userRepository: UserRepository,
	private val contactRepository: ContactRepository,
	val navMainGraphModel: NavMainGraphModel
) : BaseViewModel() {

	private val _sshKeyDownloadSuccessful = MutableSharedFlow<Boolean>(replay = 1)
	val sshKeyDownloadSuccessful = _sshKeyDownloadSuccessful.asSharedFlow()

	fun loadMyContactsKeys() {
		viewModelScope.launch(Dispatchers.IO) {
			val success = contactRepository.syncMyContactsKeys()
			_sshKeyDownloadSuccessful.emit(success)
		}
	}
}