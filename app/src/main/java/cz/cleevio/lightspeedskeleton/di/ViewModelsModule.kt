package cz.cleevio.lightspeedskeleton.di

import cz.cleevio.lightspeedskeleton.ui.forceNotificationPermission.ForceNotificationPermissionViewModel
import cz.cleevio.lightspeedskeleton.ui.mainActivity.MainViewModel
import cz.cleevio.lightspeedskeleton.ui.splashFragment.SplashViewModel
import org.koin.dsl.module

val viewModelsModule = module {

	single {
		MainViewModel(
			encryptedPreferenceRepository = get(),
			navMainGraphModel = get(),
			chatRepository = get(),
			userRepository = get(),
			contactRepository = get(),
			groupRepository = get(),
			remoteConfig = get(),
			offerRepository = get(),
			notificationUtils = get()
		)
	}

	single {
		SplashViewModel(
			userRepository = get(),
			contactRepository = get(),
			offerRepository = get(),
			chatRepository = get(),
			navMainGraphModel = get(),
			userUtils = get(),
			backgroundQueue = get(),
			encryptedPreferenceRepository = get(),
			offerUtils = get(),
			locationHelper = get(),
			encryptionUtils = get()
		)
	}

	single {
		ForceNotificationPermissionViewModel(
			navMainGraphModel = get(),
			notificationUtils = get(),
		)
	}
}