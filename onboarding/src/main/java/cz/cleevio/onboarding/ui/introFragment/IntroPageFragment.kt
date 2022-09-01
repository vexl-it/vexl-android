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
			val introAnimationId = args.getInt(INTRO_RESOURCE_ID)
			binding.introAnimation.setAnimation(introAnimationId)
			// Fixme: Uncomment play animation once the Vexl will give us smaller animations
			// binding.introAnimation.playAnimation()
		}
	}
	companion object {
		const val INTRO_TITLE = "intro_title"
		const val INTRO_RESOURCE_ID = "intro_resource_id"
	}
}
