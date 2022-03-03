package cz.cleeevio.onboarding.di

import cz.cleeevio.onboarding.codeValidationFragment.CodeValidationViewModel
import cz.cleeevio.onboarding.initPhoneFragment.InitPhoneViewModel
import cz.cleeevio.onboarding.termsFragment.TermsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val onboardingModule = module {

	viewModel {
		CodeValidationViewModel()
	}

	viewModel {
		TermsViewModel()
	}

	viewModel {
		InitPhoneViewModel(
			userRepository = get()
		)
	}
}