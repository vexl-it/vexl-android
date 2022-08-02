package cz.cleevio.onboarding.di

import cz.cleevio.core.termsFragment.TermsViewModel
import cz.cleevio.onboarding.avatarFragment.AvatarViewModel
import cz.cleevio.onboarding.initPhoneFragment.InitPhoneViewModel
import cz.cleevio.onboarding.phoneDoneFragment.PhoneDoneViewModel
import cz.cleevio.onboarding.usernameFragment.UsernameViewModel
import cz.cleevio.onboarding.verifyPhoneFragment.VerifyPhoneViewModel
import cz.cleevio.onboarding.welcomeFragment.WelcomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val onboardingModule = module {

	viewModel {
		TermsViewModel()
	}

	viewModel {
		WelcomeViewModel()
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