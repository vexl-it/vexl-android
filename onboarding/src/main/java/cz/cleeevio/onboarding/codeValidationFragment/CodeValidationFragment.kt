package cz.cleeevio.onboarding.codeValidationFragment

import androidx.lifecycle.lifecycleScope
import cz.cleeevio.onboarding.R
import cz.cleeevio.onboarding.databinding.FragmentCodeValidationBinding
import cz.cleeevio.onboarding.extensions.navigateToEmailApp
import cz.cleevio.core.utils.viewBinding
import kotlinx.coroutines.flow.collect
import lightbase.core.baseClasses.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class CodeValidationFragment : BaseFragment(R.layout.fragment_code_validation) {

	override val viewModel by viewModel<CodeValidationViewModel>()
	private val binding by viewBinding(FragmentCodeValidationBinding::bind)

	override fun bindObservers() {
		viewLifecycleOwner.lifecycleScope.launchWhenStarted {
			viewModel.resendCodeSuccessEvent.collect {
				Timber.d("Resend code success -> %s", it.toString())
			}
		}
	}

	override fun initView() {
		binding.openEmailApp.setOnClickListener {
			requireActivity().navigateToEmailApp()
		}

		setViewForTopInset(binding.container)
	}
}