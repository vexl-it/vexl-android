package cz.cleevio.lightspeedskeleton.ui.splashFragment

import androidx.navigation.fragment.findNavController
import cz.cleevio.lightspeedskeleton.R
import lightbase.core.baseClasses.BaseFragment

class SplashFragment : BaseFragment(R.layout.fragment_splash) {

	override fun initView() {
		findNavController().navigate(SplashFragmentDirections.actionToCrossroadFragment())
	}

	override fun bindObservers() = Unit
}