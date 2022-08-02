package cz.cleevio.onboarding.ui.verifyPhoneFragment

import android.widget.Toast
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.network.data.ErrorIdentification.Companion.CODE_ENTITY_NOT_EXIST_404
import cz.cleevio.network.data.Status
import cz.cleevio.onboarding.R
import cz.cleevio.onboarding.databinding.FragmentVerifyPhoneBinding
import cz.cleevio.onboarding.ui.initPhoneFragment.BOTTOM_EXTRA_PADDING
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.dpValueToPx
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import kotlinx.coroutines.delay
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.concurrent.TimeUnit

class VerifyPhoneFragment : BaseFragment(R.layout.fragment_verify_phone) {

	private val args by navArgs<VerifyPhoneFragmentArgs>()

	override val viewModel by viewModel<VerifyPhoneViewModel> { parametersOf(args.phoneNumber, args.verificationId) }
	private val binding by viewBinding(FragmentVerifyPhoneBinding::bind)

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.verificationChannel.collect { resource ->
				when (resource.status) {
					is Status.Loading -> {
						//todo: switch button to loading state
					}
					is Status.Success -> {
						if (resource.data?.phoneVerified == true) {
							//todo: switch button to verified state
							Toast.makeText(requireContext(), "Verification successful", Toast.LENGTH_SHORT)
								.show()
							delay(TimeUnit.SECONDS.toMillis(1))
							findNavController().navigate(
								VerifyPhoneFragmentDirections.proceedToPhoneDoneFragment()
							)
						} else {
							//todo: switch button to default state
						}
					}
				}
			}
		}

		repeatScopeOnStart {
			viewModel.errorFlow.collect { errorIdentification ->
				if (errorIdentification.code == CODE_ENTITY_NOT_EXIST_404) {
					viewModel.resetKeys()
					Toast.makeText(requireContext(), "Verification expired. Start you registration again please.", Toast.LENGTH_SHORT)
						.show()
					delay(TimeUnit.SECONDS.toMillis(1))
					//go back to previous screen and start again
					findNavController().popBackStack()
				}

				//todo: any other expected errors?
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
			val verificationCode = binding.verifyPhoneInput.text.toString()
			if (verificationCode.isNotBlank()) {
				viewModel.sendVerificationCode(verificationCode)
			} else {
				//todo: show toast or something
			}
		}

		binding.verifyPhoneSubtitle.text = getString(R.string.verify_phone_subtitle, args.phoneNumber)

		//debug
		binding.verifyPhoneInput.setText("111111")
	}
}
