package cz.cleevio.onboarding.welcomeFragment

import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import cz.cleevio.onboarding.databinding.FragmentWelcomeBinding
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.onboarding.R
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel

class WelcomeFragment : BaseFragment(R.layout.fragment_welcome) {

	override val viewModel by viewModel<WelcomeViewModel>()
	private val binding by viewBinding(FragmentWelcomeBinding::bind)

	override fun bindObservers() = Unit

	override fun initView() {
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top, bottom = insets.bottom)
		}

		clickableLink()

		binding.termsSwitch.setOnCheckedChangeListener { _, isChecked ->
			binding.welcomeContinueBtn.isEnabled = isChecked
		}

		binding.welcomeContinueBtn.setOnClickListener {
			findNavController().navigate(
				WelcomeFragmentDirections.proceedToInitPhoneFragment()
			)
		}
	}

	private fun clickableLink() {
		try {
			val longText = resources.getString(R.string.welcome_terms_agreement)
			val str = SpannableString(longText)
			val startIndex = longText.indexOf(resources.getString(R.string.welcome_terms_agreement_link))
			val endIndex = startIndex + resources.getString(R.string.welcome_terms_agreement_link).length

			val clickableSpan: ClickableSpan = object : ClickableSpan() {
				override fun onClick(widget: View) {
					findNavController().navigate(WelcomeFragmentDirections.proceedToTermsFragment())
				}

				override fun updateDrawState(ds: TextPaint) {
					super.updateDrawState(ds)
					ds.isUnderlineText = false
					ds.color = resources.getColor(R.color.white, null)
					ds.linkColor = resources.getColor(R.color.white, null)
				}
			}
			str.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

			binding.termsSwitchText.text = str
			binding.termsSwitchText.movementMethod = LinkMovementMethod.getInstance()
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
}