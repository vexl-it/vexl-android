package cz.cleevio.vexl.contacts.contactsListFragment

import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.base.BaseContactsListViewModel
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.repository.repository.contact.ContactRepository

class ContactsListViewModel constructor(
	private val contactRepository: ContactRepository,
	navMainGraphModel: NavMainGraphModel,
	encryptedPreferenceRepository: EncryptedPreferenceRepository
) : BaseContactsListViewModel(contactRepository, navMainGraphModel, encryptedPreferenceRepository) {

}