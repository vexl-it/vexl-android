package cz.cleeevio.onboarding.termsFragment

import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import cz.cleeevio.onboarding.R
import cz.cleeevio.onboarding.databinding.FragmentTermsBinding
import cz.cleevio.core.utils.viewBinding
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel

class TermsFragment : BaseFragment(R.layout.fragment_terms) {

	override val viewModel by viewModel<TermsViewModel>()
	private val binding by viewBinding(FragmentTermsBinding::bind)

	override fun bindObservers() = Unit

	override fun initView() {
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top, bottom = insets.bottom)
		}

		binding.termsSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
			binding.termsContinueBtn.isEnabled = isChecked
		}

		binding.termsContinueBtn.setOnClickListener {
			findNavController().navigate(
				TermsFragmentDirections.proceedToInitPhoneFragment()
			)
		}
	}
}