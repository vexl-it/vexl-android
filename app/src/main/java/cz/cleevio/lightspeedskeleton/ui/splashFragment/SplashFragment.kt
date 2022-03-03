package cz.cleevio.lightspeedskeleton.ui.splashFragment

import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import cz.cleevio.lightspeedskeleton.R
import kotlinx.coroutines.delay
import lightbase.core.baseClasses.BaseFragment

class SplashFragment : BaseFragment(R.layout.fragment_splash) {

	override fun initView() {
		lifecycle.coroutineScope.launchWhenCreated {
			delay(1500)
			findNavController().navigate(SplashFragmentDirections.actionToOnboarding())
		}

	}

	override fun bindObservers() = Unit
}