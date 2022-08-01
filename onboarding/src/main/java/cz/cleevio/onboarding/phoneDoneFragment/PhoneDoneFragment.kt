package cz.cleevio.onboarding.phoneDoneFragment

import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import cz.cleevio.onboarding.R
import cz.cleevio.onboarding.databinding.FragmentPhoneDoneBinding
import cz.cleevio.onboarding.verifyPhoneFragment.VerifyPhoneViewModel
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
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
			findNavController().navigate(
				PhoneDoneFragmentDirections.proceedToUsernameFragment()
			)
		}
	}
}