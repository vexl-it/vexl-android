package cz.cleeevio.onboarding.initPhoneFragment

import cz.cleeevio.onboarding.R
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.dpValueToPx
import lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel

const val BOTTOM_EXTRA_PADDING = 40

class InitPhoneFragment : BaseFragment(R.layout.fragment_init_phone) {

	override val viewModel by viewModel<InitPhoneViewModel>()
	private val binding by viewBinding(FragmentInitPhoneBinding::bind)

	override fun bindObservers() = Unit

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