package cz.cleeevio.onboarding.di

import cz.cleeevio.onboarding.avatarFragment.AvatarViewModel
import cz.cleeevio.onboarding.initPhoneFragment.InitPhoneViewModel
import cz.cleeevio.onboarding.phoneDoneFragment.PhoneDoneViewModel
import cz.cleeevio.onboarding.termsFragment.TermsViewModel
import cz.cleeevio.onboarding.usernameFragment.UsernameViewModel
import cz.cleeevio.onboarding.verifyPhoneFragment.VerifyPhoneViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val onboardingModule = module {

	viewModel {
		TermsViewModel()
	}

	viewModel {
		InitPhoneViewModel(
			encryptedPreferences = get(),
			userRepository = get()
		)
	}

	viewModel { (phoneNumber: String, verificationId: Long) ->
		VerifyPhoneViewModel(
			phoneNumber = phoneNumber,
			verificationId = verificationId,
			userRepository = get(),
			encryptedPreference = get(),
			contactRepository = get(),
			userUtils = get()
		)
	}

	viewModel {
		PhoneDoneViewModel()
	}

	viewModel {
		UsernameViewModel(
			userRepository = get()
		)
	}

	viewModel {
		AvatarViewModel(
			userRepository = get(),
			chatRepository = get(),
			encryptedPreference = get(),
			navMainGraphModel = get()
		)
	}
}