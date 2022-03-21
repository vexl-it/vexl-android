package cz.cleeevio.vexl.contacts.contactsListFragment

import android.content.ContentResolver
import androidx.lifecycle.viewModelScope
import cz.cleevio.core.utils.isPhoneValid
import cz.cleevio.repository.model.contact.Contact
import cz.cleevio.repository.repository.contact.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class ContactsListViewModel constructor(
	private val contactRepository: ContactRepository
) : BaseViewModel() {

	private val _notSyncedContacts = MutableStateFlow<List<Contact>>(emptyList())
	val notSyncedContacts = _notSyncedContacts.asStateFlow()

	private val _uploadSuccessful = MutableSharedFlow<Boolean>()
	val uploadSuccessful = _uploadSuccessful.asSharedFlow()

	init {
		viewModelScope.launch(Dispatchers.IO) {
			val localContacts = contactRepository.getContacts()

			val notSyncedContacts = contactRepository.checkAllContacts(
				localContacts.map { contact ->
					contact.phoneNumber
				}.filter {
					it.isNotBlank() && it.isPhoneValid()
				}
			)

			notSyncedContacts.data?.let { notSyncedPhoneNumbers ->
				_notSyncedContacts.tryEmit(localContacts.filter { contact ->
					notSyncedPhoneNumbers.contains(contact.phoneNumber)
				})
			}
		}
	}

	fun syncContacts(contentResolver: ContentResolver) {
		viewModelScope.launch(Dispatchers.IO) {
			contactRepository.syncContacts(contentResolver)
		}
	}

	fun contactSelected(contact: Contact, selected: Boolean) {
		viewModelScope.launch {
			val lastValue = _notSyncedContacts.value
			lastValue.find {
				contact.id == it.id
			}?.markedForUpload = selected
		}
	}

	fun unselectAll() {
		viewModelScope.launch {
			val newList = ArrayList<Contact>()
			_notSyncedContacts.value.forEach { contact ->
				newList.add(contact.copy().also { it.markedForUpload = false })
			}
			_notSyncedContacts.emit(newList)
		}
	}

	fun uploadAllMissingContacts() {
		viewModelScope.launch(Dispatchers.IO) {
			val result = contactRepository.uploadAllMissingContacts(
				_notSyncedContacts.value.filter {
					it.markedForUpload
				}.map {
					it.phoneNumber
				}
			)
			result.data?.let {
				_uploadSuccessful.tryEmit(it.imported)
			}
		}

	}

}