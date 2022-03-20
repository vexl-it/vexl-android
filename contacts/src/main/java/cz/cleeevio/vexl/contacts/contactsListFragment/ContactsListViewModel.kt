package cz.cleeevio.vexl.contacts.contactsListFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.core.utils.isPhoneValid
import cz.cleevio.repository.model.contact.Contact
import cz.cleevio.repository.repository.contact.ContactRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class ContactsListViewModel constructor(
	private val contactRepository: ContactRepository
) : BaseViewModel() {

	val localContacts = contactRepository.getContactsFlow()

	val _notSyncedPhoneNumbers = MutableSharedFlow<List<String>>()

	val notSyncedContacts = combine(
		localContacts,
		_notSyncedPhoneNumbers
	) { contacts, phoneNumbers ->
		return@combine contacts.filter {
			phoneNumbers.contains(it.phoneNumber)
		}
	}.distinctUntilChanged()

	init {
		viewModelScope.launch {
			localContacts.collect { contacts ->
				// TODO called twice?
				val notSyncedContacts = contactRepository.checkAllContacts(
					contacts.map { contact ->
						contact.phoneNumber
					}.filter {
						it.isNotBlank() && it.isPhoneValid()
					}
				)
				notSyncedContacts.data?.let {
					_notSyncedPhoneNumbers.tryEmit(it)
				}
			}
		}
	}

	fun contactSelected(contact: Contact, selected: Boolean) {
		viewModelScope.launch {
			val lastValue = notSyncedContacts.lastOrNull()
			lastValue?.find {
				contact == it
			}?.markedForUpload = selected
		}
	}

	fun unselectAll() {
		viewModelScope.launch {
			val lastValue = notSyncedContacts.lastOrNull()
			lastValue?.forEach {
				it.markedForUpload = false
			}
		}
	}

}