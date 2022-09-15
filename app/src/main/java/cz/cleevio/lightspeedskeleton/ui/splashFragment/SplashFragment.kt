package cz.cleevio.lightspeedskeleton.ui.splashFragment

import android.view.animation.AnimationUtils
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.core.utils.repeatScopeOnResume
import cz.cleevio.core.utils.setExitTransitionZSharedAxis
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.lightspeedskeleton.R
import cz.cleevio.lightspeedskeleton.databinding.FragmentSplashBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import kotlinx.coroutines.delay
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

const val SPLASH_DELAY = 500L

class SplashFragment : BaseFragment(R.layout.fragment_splash) {

	private val binding by viewBinding(FragmentSplashBinding::bind)
	override val viewModel by viewModel<SplashViewModel>()

	override fun initView() {
		val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
		binding.vexlGoogles.startAnimation(animation)

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

					Timber.i("Navigating to intro")
					delay(SPLASH_DELAY)
					viewModel.navMainGraphModel.navigateToGraph(
						NavMainGraphModel.NavGraph.Intro
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