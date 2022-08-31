package cz.cleevio.onboarding.ui.introFragment

import cz.cleevio.core.utils.viewBinding
import cz.cleevio.onboarding.R
import cz.cleevio.onboarding.databinding.FragmentIntroPageBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment

class IntroPageFragment : BaseFragment(R.layout.fragment_intro_page) {

	private val binding by viewBinding(FragmentIntroPageBinding::bind)

	override fun bindObservers() = Unit

	override fun initView() {
		arguments?.let { args ->
			binding.introTitle.text = args.getString(INTRO_TITLE) ?: ""
			val introImage = args.getInt(INTRO_RESOURCE_ID)
			if (introImage != NO_IMAGE) binding.introAnimation.setImageResource(introImage)
		}
	}

	companion object {
		const val INTRO_TITLE = "intro_title"
		const val INTRO_RESOURCE_ID = "intro_resource_id"
		private const val NO_IMAGE = 0
	}
}
