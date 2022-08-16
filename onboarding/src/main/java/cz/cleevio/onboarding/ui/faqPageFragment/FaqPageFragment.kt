package cz.cleevio.onboarding.ui.faqPageFragment

import cz.cleevio.core.utils.fromHtml
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.onboarding.R
import cz.cleevio.onboarding.databinding.FragmentFaqPageBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment

class FaqPageFragment : BaseFragment(R.layout.fragment_faq_page) {

	private val binding by viewBinding(FragmentFaqPageBinding::bind)

	override fun bindObservers() = Unit

	override fun initView() {
		arguments?.let { args ->
			binding.faqTitle.text = args.getString(FAQ_TITLE) ?: ""
			binding.faqSubtitle.text = fromHtml(args.getString(FAQ_SUBTITLE) ?: "")
			binding.faqImage.setImageResource(args.getInt(FAQ_RESOURCE_ID))
		}
	}

	companion object {
		const val FAQ_TITLE = "faq_title"
		const val FAQ_SUBTITLE = "faq_subtitle"
		const val FAQ_RESOURCE_ID = "faq_resource_id"
	}
}
