package cz.cleevio.onboarding.ui.termsFragment

import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import cz.cleevio.core.R
import cz.cleevio.core.databinding.FragmentTermsBinding
import cz.cleevio.core.utils.safeNavigateWithTransition
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import io.noties.markwon.Markwon
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class TermsFragment : BaseFragment(R.layout.fragment_terms) {

	override val viewModel by viewModel<TermsViewModel>()
	private val binding by viewBinding(FragmentTermsBinding::bind)

	private val markwon: Markwon by inject()

	override fun bindObservers() = Unit

	override fun initView() {
		initMarkdownText(isTermsOfUse = true)

		binding.tacClose.setOnClickListener {
			findNavController().popBackStack()
		}

		binding.termsFaqButton.setOnClickListener {
			findNavController().safeNavigateWithTransition(
				TermsFragmentDirections.proceedToFaq(continueToOnboarding = false)
			)
		}

		binding.termsPrivacyRadioGroup.setOnCheckedChangeListener { _, id ->
			when (id) {
				R.id.terms -> initMarkdownText(isTermsOfUse = true)
				R.id.privacy_policy -> initMarkdownText(isTermsOfUse = false)
				else -> {
					initMarkdownText(isTermsOfUse = true)
				}
			}
		}

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top, bottom = insets.bottom)
		}
	}

	private fun initMarkdownText(isTermsOfUse: Boolean) {
		val node = markwon.parse(
			if (isTermsOfUse) {
				getString(R.string.terms_of_use_markdown)
			} else {
				getString(R.string.terms_of_use_policy_markdown)
			}
		)
		val markdown = markwon.render(node)

		markwon.setParsedMarkdown(binding.text, markdown)
	}
}
