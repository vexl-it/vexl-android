package cz.cleeevio.vexl.contacts.finishImportFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class FinishImportViewModel constructor(
	private val userRepository: UserRepository,
	private val contactRepository: ContactRepository,
	val navMainGraphModel: NavMainGraphModel
) : BaseViewModel() {

	val user = userRepository.getUserFlow()

	fun loadMyContactsKeys() {
		viewModelScope.launch(Dispatchers.IO) {
			contactRepository.loadMyContactsKeys()
		}
	}
}