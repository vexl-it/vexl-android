package cz.cleeevio.vexl.contacts.importContactsFragment

import android.content.ContentResolver
import androidx.lifecycle.viewModelScope
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class ImportContactsViewModel constructor(
	private val userRepository: UserRepository,
	private val contactRepository: ContactRepository
) : BaseViewModel() {

	val user = userRepository.getUserFlow()
	val contacts = contactRepository.getContactsFlow()

	//todo: collect contacts here, use this to filter valid numbers
	//fixme: what to do with incomplete numbers? (missing +XXX)
//	val phoneNumberList = contactList
//		.map { it.phoneNumber.toValidPhoneNumber() }
//		.filter { it.isPhoneValid() }
//

	//todo: combine example
//	val collectedData = combine(
//		contacts.asStateFlow(),
//		todo: response data from not-imported
//	) { contacts, phoneNumbers ->
//
//		filtered = conctacs.filter { byPhoneNumber }
//
//		return filtered
//	}
//		.distinctUntilChanged()

	private val hasPermissionsChannel = Channel<Boolean>(Channel.CONFLATED)
	val hasPermissionsEvent = hasPermissionsChannel.receiveAsFlow()

	fun updateHasReadContactPermissions(hasPermissions: Boolean) {
		hasPermissionsChannel.trySend(hasPermissions)
	}

	fun syncContacts(contentResolver: ContentResolver) {
		viewModelScope.launch {
			contactRepository.syncContacts(contentResolver)
		}
	}
}