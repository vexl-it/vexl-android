package cz.cleeevio.vexl.contacts.contactsListFragment

import android.content.ContentResolver
import androidx.lifecycle.viewModelScope
import com.cleevio.vexl.cryptography.HMAC_PASSWORD
import com.cleevio.vexl.cryptography.HmacCryptoLib
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
import timber.log.Timber

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
			//get all contacts from phone
			val localContacts = contactRepository.getContacts()

			Timber.tag("ContactSync").d("Checking all contacts with the API")

			//send all contacts to BE, hashed by HMAC-256
			//BE returns list of contacts that user didn't import previously
			val notSyncedIdentifiers = contactRepository.checkAllContacts(
				localContacts.map { contact ->
					contact.phoneNumber
				}.filter {
					it.isNotBlank() && it.isPhoneValid()
				}.map { validPhoneNumber ->
					HmacCryptoLib.digest(HMAC_PASSWORD, validPhoneNumber)
				}
			)

			Timber.tag("ContactSync").d("Checking done, we have ${notSyncedIdentifiers.data?.size} not synced contacts")

			//now we take list all contacts from phone and keep only contacts that BE returned (keeping only NOT imported contacts)
			notSyncedIdentifiers.data?.let { notSyncedPhoneNumbers ->
				notSyncedContactsList = localContacts.filter { contact ->
					notSyncedPhoneNumbers.contains(
						HmacCryptoLib.digest(HMAC_PASSWORD, contact.phoneNumber)
					)
				}

				Timber.tag("ContactSync").d("All done, emiting contacts")
				//we emit those contacts to UI and show it to user
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
					Timber.tag("ASDX").d("${it.phoneNumber}")
					HmacCryptoLib.digest(HMAC_PASSWORD, it.phoneNumber)
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