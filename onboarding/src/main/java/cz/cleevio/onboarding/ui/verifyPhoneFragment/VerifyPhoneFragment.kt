package cz.cleevio.onboarding.ui.verifyPhoneFragment

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.safeNavigateWithTransition
import cz.cleevio.core.utils.showKeyboard
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.network.data.ErrorIdentification.Companion.CODE_ENTITY_NOT_EXIST_404
import cz.cleevio.network.data.Status
import cz.cleevio.onboarding.R
import cz.cleevio.onboarding.databinding.FragmentVerifyPhoneBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.hideKeyboard
import cz.cleevio.vexl.lightbase.core.extensions.listenForIMEInset
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
				if (resource.status is Status.Success) {
					if (resource.data?.phoneVerified == true) {
						findNavController().safeNavigateWithTransition(
							VerifyPhoneFragmentDirections.proceedToPhoneDoneFragment()
						)
					}
				}
				binding.progressbar.isVisible = resource.status == Status.Loading
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
		binding.close.setOnClickListener {
			findNavController().popBackStack()
		}

		binding.verifyPhoneInput.requestFocus()
		binding.verifyPhoneInput.showKeyboard()

		binding.continueBtn.setOnClickListener {
			binding.root.hideKeyboard()
			val verificationCode = binding.verifyPhoneInput.text.toString()
			viewModel.sendVerificationCode(verificationCode)
		}

		setupPhoneNumber()

		binding.verifyPhoneInput.doAfterTextChanged {
			binding.continueBtn.isEnabled = it.toString().isNotEmpty()
		}

		listenForInsets(binding.parent) { insets ->
			binding.container.updatePadding(top = insets.top)
		}

		binding.verifyPhoneInput.setText("111111")

		val defaultButtonMargin = binding.continueBtn.marginBottom
		listenForIMEInset(binding.container) { bottomInset ->
			binding.continueBtn.updateLayoutParams<ViewGroup.MarginLayoutParams> {
				bottomMargin = bottomInset + defaultButtonMargin
			}
		}
	}

	private fun setupPhoneNumber() {
		val phoneText = getString(R.string.verify_phone_subtitle, args.phoneNumber)
		val prefix = args.phoneNumber?.split(" ")?.first() ?: ""
		val spannableStringBuilder = SpannableStringBuilder(phoneText)
		spannableStringBuilder.setSpan(
			ForegroundColorSpan(Color.BLACK),
			phoneText.indexOf(prefix),
			phoneText.indexOf(prefix) + (args.phoneNumber?.length ?: 0),
			Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
		)

		binding.verifyPhoneSubtitle.text = spannableStringBuilder
	}
}
