package cz.cleevio.onboarding.di

import cz.cleevio.onboarding.ui.termsFragment.TermsViewModel
import cz.cleevio.onboarding.ui.avatarFragment.AvatarViewModel
import cz.cleevio.onboarding.ui.initPhoneFragment.InitPhoneViewModel
import cz.cleevio.onboarding.ui.phoneDoneFragment.PhoneDoneViewModel
import cz.cleevio.onboarding.ui.usernameFragment.UsernameViewModel
import cz.cleevio.onboarding.ui.verifyPhoneFragment.VerifyPhoneViewModel
import cz.cleevio.onboarding.ui.welcomeFragment.WelcomeViewModel
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
