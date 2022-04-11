package cz.cleeevio.vexl.contacts.di

import cz.cleeevio.vexl.contacts.contactsListFragment.ContactsListViewModel
import cz.cleeevio.vexl.contacts.facebookContactsListFragment.FacebookContactsListViewModel
import cz.cleeevio.vexl.contacts.importContactsFragment.ImportContactsViewModel
import cz.cleeevio.vexl.contacts.importFacebookContactsFragment.ImportFacebookContactsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val contactsModule = module {

	viewModel {
		ImportContactsViewModel(
			userRepository = get(),
			contactRepository = get()
		)
	}

	viewModel {
		ContactsListViewModel(
			get()
		)
	}

	viewModel {
		ImportFacebookContactsViewModel(
			get(),
			get(),
			get()
		)
	}

	viewModel {
		FacebookContactsListViewModel(
			get(),
			get()
		)
	}

}