package cz.cleeevio.vexl.contacts.contactsListFragment

import android.content.ContentResolver
import androidx.lifecycle.viewModelScope
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
import timber.log.Timber

class ContactsListViewModel constructor(
	private val contactRepository: ContactRepository
) : BaseViewModel() {

	private var notSyncedContactsTmp: List<Contact> = emptyList()

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
				notSyncedContactsTmp = localContacts.filter { contact ->
					notSyncedPhoneNumbers.contains(contact.phoneNumber)
				}
				_notSyncedContacts.emit(notSyncedContactsTmp)
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
			notSyncedContactsTmp.find {
				contact.id == it.id
			}?.markedForUpload = selected
		}
	}

	fun unselectAll() {
		viewModelScope.launch {
			notSyncedContactsTmp.forEach {
				it.markedForUpload = false
			}
			_notSyncedContacts.emit(ArrayList(notSyncedContactsTmp))
		}
	}

	fun uploadAllMissingContacts() {
		viewModelScope.launch(Dispatchers.IO) {
			val response = contactRepository.uploadAllMissingContacts(
				notSyncedContactsTmp.filter {
					it.markedForUpload
				}.map {
					it.phoneNumber
				}
			)
			when (response.status) {
				is Status.Success -> response.data?.let { data ->
					_uploadSuccessful.emit(data.imported)
					//todo: this is only debug, should be called on dashboard
					loadAllContacts()
				}
				is Status.Error -> _uploadSuccessful.emit(false)
			}
		}

	}

	fun loadAllContacts() {
		viewModelScope.launch(Dispatchers.IO) {
			val response = contactRepository.loadMyContactsKeys()
			when (response.status) {
				is Status.Success -> response.data?.let { data ->
					data.forEach { publicKey ->
						Timber.tag("ASDX").d(publicKey)
					}
				}
			}
		}
	}
}