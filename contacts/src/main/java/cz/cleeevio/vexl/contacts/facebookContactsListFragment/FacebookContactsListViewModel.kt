package cz.cleeevio.vexl.contacts.facebookContactsListFragment

import androidx.lifecycle.viewModelScope
import com.facebook.AccessToken
import cz.cleevio.repository.model.contact.FacebookContact
import cz.cleevio.repository.repository.contact.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel


class FacebookContactsListViewModel constructor(
	private val contactRepository: ContactRepository
) : BaseViewModel() {

	private val _notSyncedContacts = MutableStateFlow<List<FacebookContact>>(emptyList())
	val notSyncedContacts = _notSyncedContacts.asStateFlow()

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

}