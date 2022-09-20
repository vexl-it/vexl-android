package cz.cleevio.vexl.contacts.di

import cz.cleevio.vexl.contacts.contactsListFragment.ContactsListViewModel
import cz.cleevio.vexl.contacts.facebookContactsListFragment.FacebookContactsListViewModel
import cz.cleevio.vexl.contacts.finishImportFragment.FinishImportViewModel
import cz.cleevio.vexl.contacts.importContactsFragment.ImportContactsViewModel
import cz.cleevio.vexl.contacts.importFacebookContactsFragment.ImportFacebookContactsViewModel
import cz.cleevio.vexl.contacts.notificationFragment.NotificationViewModel
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
			contactRepository = get(),
			navMainGraphModel = get(),
			encryptedPreferenceRepository = get()
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

	viewModel {
		FinishImportViewModel(
			get(),
			get(),
			get()
		)
	}

	viewModel {
		NotificationViewModel()
	}

}