package cz.cleeevio.onboarding.usernameFragment

import android.widget.Toast
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import cz.cleeevio.onboarding.BuildConfig
import cz.cleeevio.onboarding.R
import cz.cleeevio.onboarding.databinding.FragmentUsernameBinding
import cz.cleeevio.onboarding.initPhoneFragment.BOTTOM_EXTRA_PADDING
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.dpValueToPx
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import kotlinx.coroutines.launch
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
					Toast.makeText(requireContext(), R.string.error_username_not_available, Toast.LENGTH_SHORT).show()
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
			if (username.isNotBlank()) {
				viewModel.checkUsernameAvailability(username)
			} else {
				Toast.makeText(requireContext(), R.string.error_nickname_blank, Toast.LENGTH_SHORT).show()
			}
		}

		//debug
		if (BuildConfig.DEBUG) {
			binding.nameNameInput.setText("jakub_test_account")
		}
	}
}