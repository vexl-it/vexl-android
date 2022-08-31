package cz.cleevio.onboarding.ui.introFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import cz.cleevio.onboarding.R
import cz.cleevio.onboarding.ui.faqPageFragment.FaqPageFragment

class IntroPagerAdapter constructor(
	private val fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

	override fun getItemCount(): Int = NUMBER_OF_PAGES

	@Suppress("MagicNumber")
	override fun createFragment(position: Int): Fragment {
		val fragment = IntroPageFragment()

		when (position) {
			0 -> fragment.arguments = Bundle().apply {
				putString(IntroPageFragment.INTRO_TITLE, fragmentActivity.getString(R.string.intro_first_title))
				putInt(IntroPageFragment.INTRO_RESOURCE_ID, R.raw.vexl_intro_1)
			}
			1 -> fragment.arguments = Bundle().apply {
				putString(IntroPageFragment.INTRO_TITLE, fragmentActivity.getString(R.string.intro_second_title))
				putInt(IntroPageFragment.INTRO_RESOURCE_ID, R.raw.vexl_intro_2)
			}
			2 -> fragment.arguments = Bundle().apply {
				putString(IntroPageFragment.INTRO_TITLE, fragmentActivity.getString(R.string.intro_third_title))
				putInt(IntroPageFragment.INTRO_RESOURCE_ID, R.raw.vexl_intro_3)
			}
			else -> error("Wrong position $position for intro")
		}

		return fragment
	}

	companion object {
		private const val NUMBER_OF_PAGES = 3
	}
}
