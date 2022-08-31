package cz.cleevio.onboarding.ui.introFragment

import cz.cleevio.core.utils.fromHtml
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.onboarding.R
import cz.cleevio.onboarding.databinding.FragmentFaqPageBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment

class IntroPageFragment : BaseFragment(R.layout.fragment_intro_page) {

	private val binding by viewBinding(FragmentFaqPageBinding::bind)

	override fun bindObservers() = Unit

	override fun initView() {
		arguments?.let { args ->
			binding.faqTitle.text = args.getString(FAQ_TITLE) ?: ""
			binding.faqSubtitle.text = fromHtml(args.getString(FAQ_SUBTITLE) ?: "")
			val faqImage = args.getInt(FAQ_RESOURCE_ID)
			if (faqImage != NO_IMAGE) binding.faqImage.setImageResource(faqImage)
		}
	}

	companion object {
		const val FAQ_TITLE = "faq_title"
		const val FAQ_SUBTITLE = "faq_subtitle"
		const val FAQ_RESOURCE_ID = "faq_resource_id"
		private const val NO_IMAGE = 0
	}
}
