package cz.cleeevio.vexl.contacts.contactsListFragment

import cz.cleevio.repository.repository.user.UserRepository
import lightbase.core.baseClasses.BaseViewModel

class ContactsListViewModel constructor(
	private val userRepository: UserRepository
) : BaseViewModel()