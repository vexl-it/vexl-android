package cz.cleevio.core.base

import android.content.ContentResolver
import androidx.lifecycle.viewModelScope
import com.cleevio.vexl.cryptography.HmacCryptoLib
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.model.OpenedFromScreen
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.network.data.Status
import cz.cleevio.repository.BuildConfig
import cz.cleevio.repository.model.contact.BaseContact
import cz.cleevio.repository.model.contact.Contact
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

open class BaseContactsListViewModel constructor(
	private val contactRepository: ContactRepository,
	val navMainGraphModel: NavMainGraphModel,
	private val encryptedPreferenceRepository: EncryptedPreferenceRepository
) : BaseViewModel() {

	private var contactsToBeShowedList: List<Contact> = emptyList()

	private val _contactsToBeShowed = MutableSharedFlow<List<Contact>>(replay = 1)
	val contactsToBeShowed = _contactsToBeShowed.asSharedFlow()

	private val _progressFlow = MutableSharedFlow<Boolean>(replay = 1)
	val progressFlow = _progressFlow.asSharedFlow()

	private val _uploadSuccessful = MutableSharedFlow<Boolean>(replay = 1)

	val successful: Flow<Boolean> = _uploadSuccessful

	private fun skipCheckingAndJustDisplayAllContactsChecked(localContacts: List<Contact>) {
		viewModelScope.launch(Dispatchers.IO) {
			_progressFlow.emit(true)
			contactsToBeShowedList = localContacts.map {it.copy(markedForUpload = true)}

			Timber.tag("ContactSync").d("All done, emitting contacts")

			emitContacts(contactsToBeShowedList)
			_progressFlow.emit(false)
		}
	}

	/**
	 * get all contacts from phone as input
	 */
	@Deprecated("This sends all contacts to server. We don't want that")
	private fun checkNotSyncedContacts(localContacts: List<Contact>) {
		viewModelScope.launch(Dispatchers.IO) {
			_progressFlow.emit(true)
			Timber.tag("ContactSync").d("We are not in onboarding flow, so we should hash all contacts")
			val hashedContacts = localContacts
				//hash all valid numbers and save them
				.map { contact ->
					contact.copy(hashedPhoneNumber = HmacCryptoLib.digest(BuildConfig.HMAC_PASSWORD, contact.phoneNumber))
				}

			//BE returns list of contacts that user didn't import previously
			Timber.tag("ContactSync").d("Checking all contacts with the API ${localContacts.joinToString(",")}")
			val contacts = contactRepository.checkAllContacts(
				//send only hashed phone numbers as identifiers
				hashedContacts.map { it.hashedPhoneNumber }
			)
			//val contacts = hashedContacts.map {it.hashedPhoneNumber}

			Timber.tag("ContactSync").d("Checking done, we have ${contacts.data?.size} not synced contacts")

			contacts.data?.let { notSyncedPhoneNumbers ->
				val newList = ArrayList<Contact>()

				hashedContacts.forEach { contact ->
					if (notSyncedPhoneNumbers.contains(contact.hashedPhoneNumber)) {
						newList.add(contact.apply { markedForUpload = false }.copy())
					} else {
						newList.add(contact.apply { markedForUpload = true }.copy())
					}
				}
				contactsToBeShowedList = newList

				Timber.tag("ContactSync").d("All done, emitting contacts")
				//we emit those contacts to UI and show it to user
				emitContacts(contactsToBeShowedList)
			}
			_progressFlow.emit(false)
		}
	}

	//get all contacts from phone as input
	private fun skipCheckingAndJustDisplayAllContacts(localContacts: List<Contact>) {
		viewModelScope.launch(Dispatchers.IO) {
			contactsToBeShowedList = localContacts

			Timber.tag("ContactSync").d("All done, emitting contacts")
			emitContacts(localContacts)
		}
	}

	fun syncContacts(contentResolver: ContentResolver, openedFromScreen: OpenedFromScreen) {
		viewModelScope.launch(Dispatchers.IO) {
			_progressFlow.emit(true)
			val response = contactRepository.syncContacts(contentResolver)
			if (response.status == Status.Success) {
				response.data?.let { localContacts ->
					if (openedFromScreen == OpenedFromScreen.ONBOARDING) {
						skipCheckingAndJustDisplayAllContacts(localContacts)
					} else {
						//checkNotSyncedContacts(localContacts)
						// We don't want to be sending all contacts to server
						skipCheckingAndJustDisplayAllContactsChecked(localContacts)
					}
				}
			} else {
				//do nothing?
			}
			_progressFlow.emit(false)
		}
	}

	fun contactSelected(contact: BaseContact, selected: Boolean) {
		viewModelScope.launch {
			contactsToBeShowedList.find {
				contact.id == it.id
			}?.markedForUpload = selected
			emitContacts(contactsToBeShowedList)
		}
	}

	fun unselectAll() {
		viewModelScope.launch {
			contactsToBeShowedList.forEach { contact ->
				contact.markedForUpload = false
			}
			emitContacts(contactsToBeShowedList)
		}
	}

	fun selectAll() {
		viewModelScope.launch {
			contactsToBeShowedList.forEach { contact ->
				contact.markedForUpload = true
			}
			emitContacts(contactsToBeShowedList)
		}
	}

	fun uploadAllMissingContacts() {
		viewModelScope.launch(Dispatchers.IO) {
			_progressFlow.emit(true)
			val contactsToBeUploaded = contactsToBeShowedList.filter { it.markedForUpload }

			val uploadResponse = contactRepository.uploadAllMissingContacts(contactsToBeUploaded)

			_progressFlow.emit(false)
			when (uploadResponse.status) {
				is Status.Success -> uploadResponse.data?.let { data ->
					_uploadSuccessful.emit(data.imported)
					encryptedPreferenceRepository.numberOfImportedContacts = contactsToBeUploaded.size
				}
				is Status.Error -> _uploadSuccessful.emit(false)
				else -> Unit
			}
		}
	}

	private suspend fun emitContacts(contacts: List<Contact>) {
		// Copying because of two lists with the same references :-(
		val newList = ArrayList<Contact>()
		contacts.forEach { contact ->
			newList.add(contact.copy())
		}
		_contactsToBeShowed.emit(newList)
	}
}