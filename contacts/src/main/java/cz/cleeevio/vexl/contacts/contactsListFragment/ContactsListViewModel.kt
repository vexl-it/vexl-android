package cz.cleeevio.vexl.contacts.contactsListFragment

import cz.cleevio.repository.repository.contact.ContactRepository
import lightbase.core.baseClasses.BaseViewModel

class ContactsListViewModel constructor(
	private val contactRepository: ContactRepository
) : BaseViewModel() {

	val contacts = contactRepository.getContactsFlow()

}