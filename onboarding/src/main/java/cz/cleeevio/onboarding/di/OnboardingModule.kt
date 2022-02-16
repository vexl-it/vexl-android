package cz.cleeevio.onboarding.di

import cz.cleeevio.onboarding.codeValidationFragment.CodeValidationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val onboardingModule = module {

	viewModel {
		CodeValidationViewModel()
	}
}