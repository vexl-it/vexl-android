package cz.cleevio.lightspeedskeleton.ui.splashFragment

import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import cz.cleevio.lightspeedskeleton.R
import kotlinx.coroutines.delay
import lightbase.core.baseClasses.BaseFragment

const val SPLASH_DELAY = 1500L

class SplashFragment : BaseFragment(R.layout.fragment_splash) {

	override fun initView() {
		lifecycle.coroutineScope.launchWhenCreated {
			delay(SPLASH_DELAY)
			findNavController().navigate(SplashFragmentDirections.actionToOnboarding())
		}
	}

	override fun bindObservers() = Unit
}