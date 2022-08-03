package cz.cleevio.onboarding.ui.phoneDoneFragment

import androidx.navigation.fragment.findNavController
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.safeNavigateWithTransition
import cz.cleevio.onboarding.R
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class PhoneDoneFragment : BaseFragment(R.layout.fragment_phone_done) {

	override val viewModel by viewModel<PhoneDoneViewModel>()

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.goToNextScreen.collect {
				if (it) {
					findNavController().safeNavigateWithTransition(
						PhoneDoneFragmentDirections.proceedToUsernameFragment(),
						clearBackStackUntil = R.id.phoneDoneFragment
					)
				}
			}
		}
	}

	override fun initView() {
		viewModel.startTimer()
	}
}
