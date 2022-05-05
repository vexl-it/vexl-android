package cz.cleeevio.vexl.contacts.contactsListFragment

import android.content.ContentResolver
import androidx.lifecycle.viewModelScope
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.core.utils.isPhoneValid
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.contact.BaseContact
import cz.cleevio.repository.model.contact.Contact
import cz.cleevio.repository.repository.contact.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class ContactsListViewModel constructor(
	private val contactRepository: ContactRepository,
	val navMainGraphModel: NavMainGraphModel
) : BaseViewModel() {

	private var notSyncedContactsList: List<Contact> = emptyList()

	private val _notSyncedContacts = MutableSharedFlow<List<Contact>>(replay = 1)
	val notSyncedContacts = _notSyncedContacts.asSharedFlow()

	private val _uploadSuccessful = MutableSharedFlow<Boolean>(replay = 1)
	val uploadSuccessful = _uploadSuccessful.asSharedFlow()

	private fun checkNotSyncedContacts() {
		viewModelScope.launch(Dispatchers.IO) {
			val localContacts = contactRepository.getContacts()

			val notSyncedIdentifiers = contactRepository.checkAllContacts(
				localContacts.map { contact ->
					contact.phoneNumber
				}.filter {
					it.isNotBlank() && it.isPhoneValid()
				}
			)

			notSyncedIdentifiers.data?.let { notSyncedPhoneNumbers ->
				notSyncedContactsList = localContacts.filter { contact ->
					notSyncedPhoneNumbers.contains(contact.phoneNumber)
				}
				emitContacts(notSyncedContactsList)
			}
		}
	}

	fun syncContacts(contentResolver: ContentResolver) {
		viewModelScope.launch(Dispatchers.IO) {
			val response = contactRepository.syncContacts(contentResolver)
			when (response.status) {
				is Status.Success -> {
					checkNotSyncedContacts()
				}
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
			val response = contactRepository.uploadAllMissingContacts(
				notSyncedContactsList.filter {
					it.markedForUpload
				}.map {
					it.phoneNumber
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

	private suspend fun emitContacts(contacts: List<Contact>) {
		// Copying because of two lists with the same references :-(
		val newList = ArrayList<Contact>()
		contacts.forEach { contact ->
			newList.add(contact.copy())
		}
		_notSyncedContacts.emit(newList)
	}
}