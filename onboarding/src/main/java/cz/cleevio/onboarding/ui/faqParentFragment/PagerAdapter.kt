package cz.cleevio.onboarding.ui.faqParentFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import cz.cleevio.onboarding.R
import cz.cleevio.onboarding.ui.faqPageFragment.FaqPageFragment

class PagerAdapter constructor(
	private val fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

	override fun getItemCount(): Int = NUMBER_OF_PAGES

	@Suppress("MagicNumber")
	override fun createFragment(position: Int): Fragment {
		val fragment = FaqPageFragment()

		when (position) {
			0 -> fragment.arguments = Bundle().apply {
				putString(FaqPageFragment.FAQ_TITLE, fragmentActivity.getString(R.string.faq_screen_one_title))
				putString(FaqPageFragment.FAQ_SUBTITLE, fragmentActivity.getString(R.string.faq_screen_one_subtitle))
				putInt(FaqPageFragment.FAQ_RESOURCE_ID, R.drawable.ic_faq_what_is_vexl)
			}
			1 -> fragment.arguments = Bundle().apply {
				putString(FaqPageFragment.FAQ_TITLE, fragmentActivity.getString(R.string.faq_screen_two_title))
				putString(FaqPageFragment.FAQ_SUBTITLE, fragmentActivity.getString(R.string.faq_screen_two_subtitle))
				putInt(FaqPageFragment.FAQ_RESOURCE_ID, R.drawable.ic_faq_contacts)
			}
			2 -> fragment.arguments = Bundle().apply {
				putString(FaqPageFragment.FAQ_TITLE, fragmentActivity.getString(R.string.faq_screen_three_title))
				putString(FaqPageFragment.FAQ_SUBTITLE, fragmentActivity.getString(R.string.faq_screen_three_subtitle))
				putInt(FaqPageFragment.FAQ_RESOURCE_ID, R.drawable.ic_faq_anonymous)
			}
			3 -> fragment.arguments = Bundle().apply {
				putString(FaqPageFragment.FAQ_TITLE, fragmentActivity.getString(R.string.faq_screen_four_title))
				putString(FaqPageFragment.FAQ_SUBTITLE, fragmentActivity.getString(R.string.faq_screen_four_subtitle))
				putInt(FaqPageFragment.FAQ_RESOURCE_ID, R.drawable.ic_faq_right_person)
			}
			4 -> fragment.arguments = Bundle().apply {
				putString(FaqPageFragment.FAQ_TITLE, fragmentActivity.getString(R.string.faq_screen_five_title))
				putString(FaqPageFragment.FAQ_SUBTITLE, fragmentActivity.getString(R.string.faq_screen_five_subtitle))
				putInt(FaqPageFragment.FAQ_RESOURCE_ID, R.drawable.ic_faq_encrypted_private_communication)
			}
			5 -> fragment.arguments = Bundle().apply {
				putString(FaqPageFragment.FAQ_TITLE, fragmentActivity.getString(R.string.faq_screen_six_title))
				putString(FaqPageFragment.FAQ_SUBTITLE, fragmentActivity.getString(R.string.faq_screen_six_subtitle))
				putInt(FaqPageFragment.FAQ_RESOURCE_ID, R.drawable.ic_faq_protected_data)
			}
			6 -> fragment.arguments = Bundle().apply {
				putString(FaqPageFragment.FAQ_TITLE, fragmentActivity.getString(R.string.faq_screen_seven_title))
				putString(
					FaqPageFragment.FAQ_SUBTITLE,
					String.format(
						CONTACT_SUBTITLE,
						fragmentActivity.getString(R.string.faq_screen_seven_subtitle_1),
						fragmentActivity.getString(R.string.faq_screen_seven_subtitle_2),
						". ${fragmentActivity.getString(R.string.faq_screen_seven_subtitle_3)}"
					).replace(IOS_NEW_LINE, ANDROID_NEW_LINE)
				)
				putInt(FaqPageFragment.FAQ_RESOURCE_ID, R.drawable.ic_faq_how_do_i_contact_vexl)
			}
			else -> error("Wrong position $position for onboarding")
		}

		return fragment
	}

	companion object {
		private const val NUMBER_OF_PAGES = 7
		private const val CONTACT_SUBTITLE = "%s <font color='#101010'><b>%s</b></font>%s"
		private const val IOS_NEW_LINE = "\n"
		private const val ANDROID_NEW_LINE = "<br>"
	}
}
