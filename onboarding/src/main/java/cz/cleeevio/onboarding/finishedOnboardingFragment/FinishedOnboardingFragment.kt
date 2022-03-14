package cz.cleeevio.onboarding.finishedOnboardingFragment

import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import coil.load
import cz.cleeevio.onboarding.R
import cz.cleeevio.onboarding.databinding.FragmentFinishedOnboardingBinding
import cz.cleeevio.onboarding.initPhoneFragment.BOTTOM_EXTRA_PADDING
import cz.cleevio.core.utils.viewBinding
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.dpValueToPx
import lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel

class FinishedOnboardingFragment : BaseFragment(R.layout.fragment_finished_onboarding) {

	private val binding by viewBinding(FragmentFinishedOnboardingBinding::bind)
	override val viewModel by viewModel<FinishedOnboardingViewModel>()

	override fun bindObservers() {

		viewLifecycleOwner.lifecycleScope.launch {
			viewModel.user.collect { user ->
				binding.username.text = user?.username
				binding.avatarImage.load(user?.avatar)
			}
		}

	}

	override fun initView() {

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottomWithIME + requireContext().dpValueToPx(BOTTOM_EXTRA_PADDING).toInt()
			)
		}

		viewModel.getUserData()

	}

}