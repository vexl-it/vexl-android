package cz.cleeevio.onboarding.initPhoneFragment

import androidx.core.view.updatePadding
import cz.cleeevio.onboarding.R
import cz.cleeevio.onboarding.databinding.FragmentInitPhoneBinding
import cz.cleevio.core.utils.viewBinding
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.dpValueToPx
import lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class InitPhoneFragment : BaseFragment(R.layout.fragment_init_phone) {

	override val viewModel by viewModel<InitPhoneViewModel>()

	private val binding by viewBinding(FragmentInitPhoneBinding::bind)

	override fun bindObservers() {

	}

	override fun initView() {

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottomWithIME + requireContext().dpValueToPx(40).toInt()
			)
		}

		binding.termsContinueBtn.setOnClickListener {
			val phoneNumber = binding.initPhoneInput.text.toString()
			Timber.tag("ASDX").d("Input phone: $phoneNumber")
			viewModel.sendPhoneNumber(phoneNumber)
		}

		viewModel.sendPhoneNumber("+420602000000")
	}
}