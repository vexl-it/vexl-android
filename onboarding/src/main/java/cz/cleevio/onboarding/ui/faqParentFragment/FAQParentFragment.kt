package cz.cleevio.onboarding.ui.faqParentFragment

import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.onboarding.R
import cz.cleevio.onboarding.databinding.FragmentFaqParentBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets

class FAQParentFragment : BaseFragment(R.layout.fragment_faq_parent) {

	private val binding by viewBinding(FragmentFaqParentBinding::bind)

	override val hasMenu = true

	override fun bindObservers() = Unit

	private var adapter: PagerAdapter? = null

	override fun initView() {
		setupAdapter()

		binding.close.setOnClickListener {
			findNavController().popBackStack()
		}

		binding.faqParentLeftBtn.setOnClickListener {
			when (binding.faqViewpager.currentItem) {
				0 -> findNavController().popBackStack()
				else -> binding.faqViewpager.setCurrentItem(binding.faqViewpager.currentItem - 1, true)
			}
		}
		binding.faqParentRightBtn.setOnClickListener {
			when (binding.faqViewpager.currentItem) {
				(adapter?.itemCount ?: 0) - 1 -> findNavController().popBackStack()
				else -> binding.faqViewpager.setCurrentItem(binding.faqViewpager.currentItem + 1, true)
			}
		}

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top, bottom = insets.bottom)
		}
	}

	private fun setupAdapter() {
		adapter = PagerAdapter(requireActivity())
		binding.faqViewpager.adapter = adapter
		binding.faqViewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
			override fun onPageSelected(position: Int) {
				binding.segmentBar.setProgress(position + 1)

				when (position) {
					0 -> {
						binding.faqParentLeftBtn.text = getString(R.string.general_close)
						binding.faqParentRightBtn.text = getString(R.string.next)
					}
					(adapter?.itemCount ?: 0) - 1 -> {
						binding.faqParentLeftBtn.text = getString(R.string.back)
						binding.faqParentRightBtn.text = getString(R.string.general_done)
					}
					else -> {
						binding.faqParentLeftBtn.text = getString(R.string.back)
						binding.faqParentRightBtn.text = getString(R.string.next)
					}
				}

				super.onPageSelected(position)
			}
		})
	}
}
