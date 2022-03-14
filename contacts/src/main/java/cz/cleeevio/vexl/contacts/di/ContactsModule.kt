package cz.cleeevio.vexl.contacts.di

import cz.cleeevio.vexl.contacts.importContactsFragment.ImportContactsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val contactsModule = module {

	viewModel {
		ImportContactsViewModel(
			userRepository = get()
		)
	}

}