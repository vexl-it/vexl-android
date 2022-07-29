package cz.cleevio.vexl.contacts.importContactsFragment

import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.user.UserRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class ImportContactsViewModel constructor(
	private val userRepository: UserRepository,
	private val contactRepository: ContactRepository
) : BaseViewModel() {

	private val hasPermissionsChannel = Channel<Boolean>(Channel.CONFLATED)
	val hasPermissionsEvent = hasPermissionsChannel.receiveAsFlow()

	fun updateHasReadContactPermissions(hasPermissions: Boolean) {
		hasPermissionsChannel.trySend(hasPermissions)
	}
}