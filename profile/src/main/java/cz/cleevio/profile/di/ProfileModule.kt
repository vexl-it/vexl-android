package cz.cleevio.profile.di

import cz.cleevio.profile.profileFragment.ProfileViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {

	viewModel {
		ProfileViewModel()
	}
}