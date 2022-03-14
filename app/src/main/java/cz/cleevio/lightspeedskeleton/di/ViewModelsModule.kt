package cz.cleevio.lightspeedskeleton.di

import cz.cleevio.lightspeedskeleton.ui.mainActivity.MainViewModel
import org.koin.dsl.module

val viewModelsModule = module {

	single {
		MainViewModel(
			navMainGraphModel = get(),
			userRepository = get()
		)
	}
}