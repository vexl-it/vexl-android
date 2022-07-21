package cz.cleevio.profile.di

import cz.cleevio.profile.groupFragment.GroupViewModel
import cz.cleevio.profile.profileFragment.ProfileViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {

	viewModel {
		ProfileViewModel(
			userRepository = get(),
			contactRepository = get(),
			offerRepository = get(),
			navMainGraphModel = get()
		)
	}

	viewModel {
		GroupViewModel(
			groupRepository = get(),
		)
	}
}