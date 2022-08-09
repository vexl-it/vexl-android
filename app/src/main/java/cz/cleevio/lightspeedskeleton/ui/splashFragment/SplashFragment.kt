package cz.cleevio.lightspeedskeleton.ui.splashFragment

import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.core.utils.setExitTransitionZSharedAxis
import cz.cleevio.core.utils.repeatScopeOnResume
import cz.cleevio.lightspeedskeleton.R
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import kotlinx.coroutines.delay
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

const val SPLASH_DELAY = 500L

class SplashFragment : BaseFragment(R.layout.fragment_splash) {

	override val viewModel by viewModel<SplashViewModel>()

	override fun initView() {
		setExitTransitionZSharedAxis()
	}

	override fun bindObservers() {
		repeatScopeOnResume {
			viewModel.userFlow.collect { user ->
				if (user != null && user.finishedOnboarding) {
					Timber.i("Navigating to marketplace")
					viewModel.loadMyContactsKeys()
				} else {
					// continue to onboarding
					viewModel.deletePreviousUserKeys()

					Timber.i("Navigating to onboarding")
					delay(SPLASH_DELAY)
					viewModel.navMainGraphModel.navigateToGraph(
						NavMainGraphModel.NavGraph.Onboarding
					)
				}
			}
		}

		repeatScopeOnResume {
			viewModel.contactKeysLoaded.collect {
				viewModel.navMainGraphModel.navigateToGraph(
					NavMainGraphModel.NavGraph.Main
				)
			}
		}
	}
}