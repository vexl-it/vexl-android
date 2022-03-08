package cz.cleeevio.onboarding.phoneDoneFragment

import androidx.core.view.updatePadding
import cz.cleeevio.onboarding.R
import cz.cleeevio.onboarding.databinding.FragmentPhoneDoneBinding
import cz.cleeevio.onboarding.verifyPhoneFragment.VerifyPhoneViewModel
import cz.cleevio.core.utils.viewBinding
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel

class PhoneDoneFragment : BaseFragment(R.layout.fragment_phone_done) {

	override val viewModel by viewModel<VerifyPhoneViewModel>()
	private val binding by viewBinding(FragmentPhoneDoneBinding::bind)

	override fun bindObservers() = Unit

	override fun initView() {
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top, bottom = insets.bottom)
		}

		binding.continueBtn.setOnClickListener {
			//todo: connect to Name Fragment here?
		}
	}
}