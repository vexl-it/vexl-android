package cz.cleevio.lightspeedskeleton.di

import cz.cleevio.lightspeedskeleton.ui.mainActivity.MainViewModel
import cz.cleevio.lightspeedskeleton.ui.splashFragment.SplashViewModel
import org.koin.dsl.module

val viewModelsModule = module {

	single {
		MainViewModel(
			encryptedPreferenceRepository = get(),
			navMainGraphModel = get(),
			chatRepository = get(),
		)
	}

	single {
		SplashViewModel(
			userRepository = get(),
			contactRepository = get(),
			offerRepository = get(),
			chatRepository = get(),
			navMainGraphModel = get(),
			userUtils = get()
		)
	}
}