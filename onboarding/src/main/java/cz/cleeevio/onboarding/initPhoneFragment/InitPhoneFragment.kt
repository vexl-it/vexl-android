package cz.cleeevio.onboarding.initPhoneFragment

import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import cz.cleeevio.onboarding.R
import cz.cleeevio.onboarding.databinding.FragmentInitPhoneBinding
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.dpValueToPx
import lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel

const val BOTTOM_EXTRA_PADDING = 40

class InitPhoneFragment : BaseFragment(R.layout.fragment_init_phone) {
	override val viewModel by viewModel<InitPhoneViewModel>()
	private val binding by viewBinding(FragmentInitPhoneBinding::bind)

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.phoneNumberSuccess.collect { initPhoneSuccess ->
				findNavController().navigate(
					InitPhoneFragmentDirections.proceedToVerifyPhoneFragment(
						initPhoneSuccess.phoneNumber, initPhoneSuccess.confirmPhone.verificationId
					)
				)
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

		binding.termsContinueBtn.setOnClickListener {
			val phoneNumber = binding.initPhoneInput.text.toString()
			viewModel.sendPhoneNumber(phoneNumber)
		}

		//debug
		binding.initPhoneInput.setText("+420602000000")
	}
}