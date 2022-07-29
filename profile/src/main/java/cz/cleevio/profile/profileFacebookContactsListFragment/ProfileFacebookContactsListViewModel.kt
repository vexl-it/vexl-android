package cz.cleevio.profile.profileFacebookContactsListFragment

import cz.cleevio.core.base.BaseFacebookContactsListViewModel
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.repository.repository.contact.ContactRepository


class ProfileFacebookContactsListViewModel constructor(
	private val contactRepository: ContactRepository,
	navMainGraphModel: NavMainGraphModel
) : BaseFacebookContactsListViewModel(contactRepository, navMainGraphModel) {

}