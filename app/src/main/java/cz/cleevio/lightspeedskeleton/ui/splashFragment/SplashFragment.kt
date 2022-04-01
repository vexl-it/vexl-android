package cz.cleevio.lightspeedskeleton.ui.splashFragment

import androidx.lifecycle.coroutineScope
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.lightspeedskeleton.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

const val SPLASH_DELAY = 1500L

class SplashFragment : BaseFragment(R.layout.fragment_splash) {

	override val viewModel by viewModel<SplashViewModel>()

	override fun initView() {}

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.userFlow.collect { user ->

				if (user != null) {
					lifecycle.coroutineScope.launch {
						Timber.i("Navigating to marketplace")
						delay(SPLASH_DELAY)
						viewModel.navMainGraphModel.navigateToGraph(
							NavMainGraphModel.NavGraph.Marketplace
						)
					}
				} else {
					//debug: delete previous user, load new keys, continue to onboarding
					viewModel.deletePreviousUserAndLoadKeys()
				}
			}
		}

		repeatScopeOnStart {
			viewModel.keysLoaded.collect { success ->
				if (success) {
					lifecycle.coroutineScope.launch {
						Timber.i("Navigating to onboarding")
						delay(SPLASH_DELAY)
						viewModel.navMainGraphModel.navigateToGraph(
							NavMainGraphModel.NavGraph.Onboarding
						)
					}
				} else {
					//todo: inform user, or try again, or something
				}
			}
		}
	}
}