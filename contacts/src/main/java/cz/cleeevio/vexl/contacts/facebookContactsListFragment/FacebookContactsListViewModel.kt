package cz.cleeevio.vexl.contacts.facebookContactsListFragment

import androidx.lifecycle.viewModelScope
import com.facebook.AccessToken
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.contact.BaseContact
import cz.cleevio.repository.model.contact.FacebookContact
import cz.cleevio.repository.repository.contact.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel


class FacebookContactsListViewModel constructor(
	private val contactRepository: ContactRepository,
	val navMainGraphModel: NavMainGraphModel
) : BaseViewModel() {

	private var notSyncedContactsList: List<FacebookContact> = emptyList()

	private val _notSyncedContacts = MutableSharedFlow<List<FacebookContact>>(replay = 1)
	val notSyncedContacts = _notSyncedContacts.asSharedFlow()

	private val _uploadSuccessful = MutableSharedFlow<Boolean>()
	val uploadSuccessful = _uploadSuccessful.asSharedFlow()

	fun loadNotSyncedFacebookContacts() {
		AccessToken.getCurrentAccessToken()?.let {
			loadNotSyncedFacebookContacts(
				it.userId,
				it.token
			)
		}
	}

	private fun loadNotSyncedFacebookContacts(facebookId: String, accessToken: String) {
		viewModelScope.launch(Dispatchers.IO) {
			val response = contactRepository.syncFacebookContacts(facebookId, accessToken)
			response.data?.let {
				notSyncedContactsList = it
				emitContacts(notSyncedContactsList)
			}
		}
	}

	fun contactSelected(contact: BaseContact, selected: Boolean) {
		viewModelScope.launch {
			notSyncedContactsList.find {
				contact.id == it.id
			}?.markedForUpload = selected
			emitContacts(notSyncedContactsList)
		}
	}

	fun unselectAll() {
		viewModelScope.launch {
			notSyncedContactsList.forEach { contact ->
				contact.markedForUpload = false
			}
			emitContacts(notSyncedContactsList)
		}
	}

	fun uploadAllMissingContacts() {
		viewModelScope.launch(Dispatchers.IO) {
			val response = contactRepository.uploadAllMissingFBContacts(
				notSyncedContactsList.filter {
					it.markedForUpload
				}.map {
					it.id
				}
			)
			when (response.status) {
				is Status.Success -> response.data?.let { data ->
					_uploadSuccessful.emit(data.imported)
				}
				is Status.Error -> _uploadSuccessful.emit(false)
			}
		}

	}

	private suspend fun emitContacts(contacts: List<FacebookContact>) {
		// Copying because of two lists with the same references :-(
		val newList = ArrayList<FacebookContact>()
		contacts.forEach { contact ->
			newList.add(contact.copy())
		}
		_notSyncedContacts.emit(newList)
	}

}