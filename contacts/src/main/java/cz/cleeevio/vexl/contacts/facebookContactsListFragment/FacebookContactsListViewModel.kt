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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel


class FacebookContactsListViewModel constructor(
	private val contactRepository: ContactRepository,
	val navMainGraphModel: NavMainGraphModel
) : BaseViewModel() {

	private val _notSyncedContacts = MutableStateFlow<List<FacebookContact>>(emptyList())
	val notSyncedContacts = _notSyncedContacts.asStateFlow()

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
			val response = contactRepository.getFacebookContacts(facebookId, accessToken)
			response.data?.let {
				_notSyncedContacts.emit(it)
			}
		}
	}

	fun contactSelected(contact: BaseContact, selected: Boolean) {
		viewModelScope.launch {
			val lastValue = _notSyncedContacts.value
			lastValue.find {
				contact.id == it.id
			}?.markedForUpload = selected
		}
	}

	fun unselectAll() {
		viewModelScope.launch {
			val newList = ArrayList<FacebookContact>()
			_notSyncedContacts.value.forEach { contact ->
				newList.add(contact.copy().also { it.markedForUpload = false })
			}
			_notSyncedContacts.emit(newList)
		}
	}

	fun uploadAllMissingContacts() {
		viewModelScope.launch(Dispatchers.IO) {
			val response = contactRepository.uploadAllMissingContacts(
				_notSyncedContacts.value.filter {
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

}