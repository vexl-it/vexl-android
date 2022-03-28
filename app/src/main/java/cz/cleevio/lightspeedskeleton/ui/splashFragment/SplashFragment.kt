package cz.cleevio.lightspeedskeleton.ui.splashFragment

import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.lightspeedskeleton.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

const val SPLASH_DELAY = 1500L

class SplashFragment : BaseFragment(R.layout.fragment_splash) {

	override val viewModel by viewModel<SplashViewModel>()

	override fun initView() {
		viewModel.deletePreviousUser() //debug only todo: remove
		viewModel.resetKeys()    //debug only todo: key management should be moved to init phone or confirm phone fragment
		viewModel.loadKeys()
	}

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.keysLoaded.collect { success ->
				if (success) {
					lifecycle.coroutineScope.launch {
						delay(SPLASH_DELAY)
						findNavController().navigate(SplashFragmentDirections.actionToOnboarding())
						//findNavController().navigate(SplashFragmentDirections.actionGlobalToContacts())
					}
				} else {
					//todo: inform user, or try again, or something
				}
			}
		}
	}
}