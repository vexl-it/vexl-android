package cz.cleevio.onboarding.ui.usernameFragment

import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.safeNavigateWithTransition
import cz.cleevio.core.utils.showKeyboard
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.onboarding.R
import cz.cleevio.onboarding.databinding.FragmentUsernameBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.hideKeyboard
import cz.cleevio.vexl.lightbase.core.extensions.listenForIMEInset
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel

class UsernameFragment : BaseFragment(R.layout.fragment_username) {

	override val viewModel by viewModel<UsernameViewModel>()
	private val binding by viewBinding(FragmentUsernameBinding::bind)

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.usernameAvailable.collect { usernameAvailable ->
				if (usernameAvailable.available) {
					findNavController().safeNavigateWithTransition(
						UsernameFragmentDirections.proceedToAvatarFragment(usernameAvailable.username)
					)
				} else {
					Toast.makeText(requireContext(), R.string.error_username_not_available, Toast.LENGTH_SHORT).show()
				}
			}
		}

		repeatScopeOnStart {
			viewModel.loading.collect {
				binding.continueBtn.isEnabled = !it
				binding.progressbar.isVisible = it
			}
		}
	}

	override fun initView() {
		binding.usernameInput.requestFocus()
		binding.usernameInput.showKeyboard()

		binding.continueBtn.setOnClickListener {
			binding.usernameInput.hideKeyboard()
			val username = binding.usernameInput.text.toString()
			viewModel.checkUsernameAvailability(username)
		}

		binding.usernameInput.doAfterTextChanged {
			binding.continueBtn.isEnabled = it.toString().isNotEmpty()
		}

		listenForInsets(binding.parent) { insets ->
			binding.container.updatePadding(top = insets.top)
		}

		val defaultButtonMargin = binding.continueBtn.marginBottom
		listenForIMEInset(binding.container) { bottomInset ->
			binding.continueBtn.updateLayoutParams<ViewGroup.MarginLayoutParams> {
				bottomMargin = bottomInset + defaultButtonMargin
			}
		}
	}
}
