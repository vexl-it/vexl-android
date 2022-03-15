package cz.cleeevio.vexl.contacts.importContactsFragment

import cz.cleevio.repository.repository.user.UserRepository
import lightbase.core.baseClasses.BaseViewModel

class ImportContactsViewModel constructor(
	private val userRepository: UserRepository
) : BaseViewModel() {

	val user = userRepository.getUserFlow()

}