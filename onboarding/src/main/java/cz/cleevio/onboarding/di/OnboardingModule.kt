package cz.cleevio.onboarding.di

import cz.cleevio.onboarding.ui.anonymizeUserFragment.AnonymizeUserViewModel
import cz.cleevio.onboarding.ui.createAvatarFragment.CreateAvatarViewModel
import cz.cleevio.onboarding.ui.initPhoneFragment.InitPhoneViewModel
import cz.cleevio.onboarding.ui.phoneDoneFragment.PhoneDoneViewModel
import cz.cleevio.onboarding.ui.termsFragment.TermsViewModel
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

	viewModel { (phoneNumber: String, verificationId: Long, expirationAt: String) ->
		VerifyPhoneViewModel(
			phoneNumber = phoneNumber,
			initialVerificationId = verificationId,
			initialExpirationAt = expirationAt,
			userRepository = get(),
			encryptedPreference = get(),
			contactRepository = get(),
			userUtils = get()
		)
	}

	viewModel {
		PhoneDoneViewModel(
			navMainGraphModel = get()
		)
	}

	viewModel {
		CreateAvatarViewModel(
			navMainGraphModel = get(),
			imageHelper = get()
		)
	}

	viewModel {
		AnonymizeUserViewModel(
			userRepository = get(),
			navMainGraphModel = get(),
			chatRepository = get(),
			encryptedPreference = get(),
		)
	}
}
