package cz.cleeevio.onboarding.usernameFragment

import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import cz.cleeevio.onboarding.R
import cz.cleeevio.onboarding.databinding.FragmentUsernameBinding
import cz.cleeevio.onboarding.initPhoneFragment.BOTTOM_EXTRA_PADDING
import cz.cleevio.core.utils.viewBinding
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.dpValueToPx
import lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel

class UsernameFragment : BaseFragment(R.layout.fragment_username) {

	override val viewModel by viewModel<UsernameViewModel>()
	private val binding by viewBinding(FragmentUsernameBinding::bind)

	override fun bindObservers() {
		viewLifecycleOwner.lifecycleScope.launch {
			viewModel.usernameAvailable.collect { usernameAvailable ->
				if (usernameAvailable.available) {
					findNavController().navigate(
						UsernameFragmentDirections.proceedToAvatarFragment(usernameAvailable.username)
					)
				} else {
					// TODO some error message?
				}
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

		binding.continueBtn.setOnClickListener {
			val username = binding.nameNameInput.text.toString()
			viewModel.checkUsernameAvailability(username)
		}
	}
}