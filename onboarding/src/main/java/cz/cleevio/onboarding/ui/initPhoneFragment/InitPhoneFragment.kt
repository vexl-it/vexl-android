package cz.cleevio.onboarding.ui.initPhoneFragment

import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.safeNavigateWithTransition
import cz.cleevio.core.utils.showKeyboard
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.onboarding.R
import cz.cleevio.onboarding.databinding.FragmentInitPhoneBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.hideKeyboard
import cz.cleevio.vexl.lightbase.core.extensions.listenForIMEInset
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class InitPhoneFragment : BaseFragment(R.layout.fragment_init_phone) {

	override val viewModel by viewModel<InitPhoneViewModel>()
	private val binding by viewBinding(FragmentInitPhoneBinding::bind)

	private lateinit var phoneNumberUtil: PhoneNumberUtil
	private var textWatcher: TextWatcher? = null

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.phoneNumberSuccess.collect { initPhoneSuccess ->
				findNavController().safeNavigateWithTransition(
					InitPhoneFragmentDirections.proceedToVerifyPhoneFragment(
						initPhoneSuccess.phoneNumber, initPhoneSuccess.confirmPhone.verificationId
					)
				)
			}
		}

		repeatScopeOnStart {
			viewModel.loading.collect { loading ->
				binding.termsContinueBtn.isEnabled = !loading
				binding.progressbar.isVisible = loading
			}
		}
	}

	override fun initView() {
		phoneNumberUtil = PhoneNumberUtil.createInstance(requireContext())

		binding.initPhoneInput.requestFocus()
		binding.initPhoneInput.showKeyboard()

		binding.termsContinueBtn.setOnClickListener {
			binding.root.hideKeyboard()
			val phoneNumber = binding.initPhoneInput.text.toString()
			viewModel.sendPhoneNumber(phoneNumber)
		}

		textWatcher = object : TextWatcher {
			override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
			override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
			override fun afterTextChanged(s: Editable?) {
				val number = s.toString()
				val countryIsoCode = getCountryIsoCode(number)
				if (countryIsoCode == null) {
					setTextNormally(number)
				} else {
					setEmojiAndColorize(countryIsoCode, number)
				}
				binding.initPhoneInput.setSelection(binding.initPhoneInput.length())
			}
		}

		binding.initPhoneInput.addTextChangedListener(textWatcher)

		listenForInsets(binding.parent) { insets ->
			binding.container.updatePadding(top = insets.top)
		}

		val defaultButtonMargin = binding.termsContinueBtn.marginBottom
		listenForIMEInset(binding.container) { bottomInset ->
			binding.termsContinueBtn.updateLayoutParams<ViewGroup.MarginLayoutParams> {
				bottomMargin = bottomInset + defaultButtonMargin
			}
		}
	}

	private fun setEmojiAndColorize(countryIsoCode: String, number: String) {
		binding.emoji.text = countryIsoCode.toFlagEmoji()
		binding.initPhoneInput.removeTextChangedListener(textWatcher)
		val newPhoneNumber = PhoneNumberUtils.formatNumber(number, countryIsoCode)
		val sb = SpannableStringBuilder(newPhoneNumber)
		sb.setSpan(
			ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.gray_4)),
			0, phoneNumberUtil.getCountryCodeForRegion(countryIsoCode).toString().length + 1,
			Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
		)

		binding.initPhoneInput.text = sb
		binding.initPhoneInput.addTextChangedListener(textWatcher)
	}

	private fun setTextNormally(number: String) {
		binding.initPhoneInput.removeTextChangedListener(textWatcher)
		binding.initPhoneInput.setText(number)
		binding.initPhoneInput.addTextChangedListener(textWatcher)
	}

	private fun getCountryIsoCode(number: String): String? {
		val validatedNumber = if (number.startsWith("+")) number else "+$number"

		val phoneNumber = try {
			phoneNumberUtil.parse(validatedNumber, null)
		} catch (e: NumberParseException) {
			Timber.e(e, "error during parsing a number")
			null
		} ?: return null

		return phoneNumberUtil.getRegionCodeForCountryCode(phoneNumber.countryCode)
	}
}

@Suppress("MagicNumber")
private fun String.toFlagEmoji(): String {
	try {
		if (this.length != 2) {
			return this
		}

		val countryCodeCaps = this.uppercase() // upper case is important because we are calculating offset
		val firstLetter = Character.codePointAt(countryCodeCaps, 0) - 0x41 + 0x1F1E6
		val secondLetter = Character.codePointAt(countryCodeCaps, 1) - 0x41 + 0x1F1E6

		// 2. It then checks if both characters are alphabet
		if (!countryCodeCaps[0].isLetter() || !countryCodeCaps[1].isLetter()) {
			return this
		}

		return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))

	} catch (e: Exception) {
		Timber.e(e)
		return ""
	}
}
