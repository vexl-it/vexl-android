package cz.cleevio.core.base

import android.content.ContentResolver
import androidx.lifecycle.viewModelScope
import com.cleevio.vexl.cryptography.HMAC_PASSWORD
import com.cleevio.vexl.cryptography.HmacCryptoLib
import cz.cleevio.core.model.OpenedFromScreen
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.core.utils.isPhoneValid
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.contact.BaseContact
import cz.cleevio.repository.model.contact.Contact
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

open class BaseContactsListViewModel constructor(
	private val contactRepository: ContactRepository,
	val navMainGraphModel: NavMainGraphModel
) : BaseViewModel() {

	private var notSyncedContactsList: List<Contact> = emptyList()

	private val _notSyncedContacts = MutableSharedFlow<List<Contact>>(replay = 1)
	val notSyncedContacts = _notSyncedContacts.asSharedFlow()

	private val _uploadSuccessful = MutableSharedFlow<Boolean>(replay = 1)
	val uploadSuccessful = _uploadSuccessful.asSharedFlow()

	private val _progressFlow = MutableSharedFlow<Boolean>(replay = 1)
	val progressFlow = _progressFlow.asSharedFlow()

	//get all contacts from phone as input
	private fun checkNotSyncedContacts(localContacts: List<Contact>) {
		viewModelScope.launch(Dispatchers.IO) {
			_progressFlow.emit(true)
			Timber.tag("ContactSync").d("We are not in onboarding flow, so we should hash all contacts")
			val hashedContacts = localContacts
				//filter out invalid phone numbers
				.filter {
					it.phoneNumber.isNotBlank() && it.phoneNumber.isPhoneValid()
				}
				//hash all valid numbers and save them
				.map { contact ->
					contact.copy(hashedPhoneNumber = HmacCryptoLib.digest(HMAC_PASSWORD, contact.phoneNumber))
				}

			//BE returns list of contacts that user didn't import previously
			Timber.tag("ContactSync").d("Checking all contacts with the API")
			val notSyncedIdentifiers = contactRepository.checkAllContacts(
				//send only hashed phone numbers as identifiers
				hashedContacts.map { it.hashedPhoneNumber }
			)

			Timber.tag("ContactSync").d("Checking done, we have ${notSyncedIdentifiers.data?.size} not synced contacts")

			//now we take list all contacts from phone and keep only contacts that BE returned (keeping only NOT imported contacts)
			notSyncedIdentifiers.data?.let { notSyncedPhoneNumbers ->
				notSyncedContactsList = hashedContacts.filter { contact ->
					notSyncedPhoneNumbers.contains(
						contact.hashedPhoneNumber
					)
				}

				Timber.tag("ContactSync").d("All done, emitting contacts")
				//we emit those contacts to UI and show it to user
				emitContacts(notSyncedContactsList)
			}
			_progressFlow.emit(false)
		}
	}

	//get all contacts from phone as input
	private fun skipCheckingAndJustDisplayAllContacts(localContacts: List<Contact>) {
		viewModelScope.launch(Dispatchers.IO) {
			notSyncedContactsList = localContacts

			Timber.tag("ContactSync").d("All done, emitting contacts")
			emitContacts(localContacts)
		}
	}

	fun syncContacts(contentResolver: ContentResolver, openedFromScreen: OpenedFromScreen) {
		viewModelScope.launch(Dispatchers.IO) {
			_progressFlow.emit(true)
			val response = contactRepository.syncContacts(contentResolver)
			when (response.status) {
				is Status.Success -> {
					response.data?.let { localContacts ->
						if (openedFromScreen == OpenedFromScreen.ONBOARDING) {
							skipCheckingAndJustDisplayAllContacts(localContacts)
						} else {
							checkNotSyncedContacts(localContacts)
						}
					}
				}
				else -> {
					//do nothing?
				}
			}
			_progressFlow.emit(false)
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
			_progressFlow.emit(true)
			val response = contactRepository.uploadAllMissingContacts(
				notSyncedContactsList.filter {
					it.markedForUpload
				}
			)
			_progressFlow.emit(false)
			when (response.status) {
				is Status.Success -> response.data?.let { data ->
					_uploadSuccessful.emit(data.imported)
				}
				is Status.Error -> _uploadSuccessful.emit(false)
				else -> {
					//do nothing?
				}
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