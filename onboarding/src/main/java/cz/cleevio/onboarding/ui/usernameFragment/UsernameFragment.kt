package cz.cleevio.onboarding.ui.usernameFragment

import android.view.ViewGroup
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import cz.cleevio.core.utils.safeNavigateWithTransition
import cz.cleevio.core.utils.setDebouncedOnClickListener
import cz.cleevio.core.utils.showKeyboard
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.onboarding.R
import cz.cleevio.onboarding.databinding.FragmentUsernameBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.hideKeyboard
import cz.cleevio.vexl.lightbase.core.extensions.listenForIMEInset
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets

class UsernameFragment : BaseFragment(R.layout.fragment_username) {

	private val binding by viewBinding(FragmentUsernameBinding::bind)

	override fun bindObservers() = Unit

	override fun initView() {
		binding.usernameInput.requestFocus()
		binding.usernameInput.showKeyboard()

		binding.close.setOnClickListener {
			findNavController().popBackStack()
		}

		binding.continueBtn.setDebouncedOnClickListener {
			binding.usernameInput.hideKeyboard()
			val username = binding.usernameInput.text.toString()
			findNavController().safeNavigateWithTransition(
				UsernameFragmentDirections.proceedToAvatarFragment(username)
			)
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
