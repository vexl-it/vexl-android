package cz.cleevio.profile.profileContactsListFragment

import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.base.BaseContactsListViewModel
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.repository.repository.contact.ContactRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class ProfileContactsListViewModel constructor(
	private val contactRepository: ContactRepository,
	navMainGraphModel: NavMainGraphModel,
	encryptedPreferenceRepository: EncryptedPreferenceRepository
) : BaseContactsListViewModel(contactRepository, navMainGraphModel, encryptedPreferenceRepository) {

}