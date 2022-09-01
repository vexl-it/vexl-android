package cz.cleevio.onboarding.ui.introFragment

import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import cz.cleevio.core.utils.safeNavigateWithTransition
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.onboarding.R
import cz.cleevio.onboarding.databinding.FragmentIntroParentBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets

class IntroParentFragment : BaseFragment(R.layout.fragment_intro_parent) {

	private val binding by viewBinding(FragmentIntroParentBinding::bind)
	override val hasMenu = true

	private var adapter: IntroPagerAdapter? = null

	override fun bindObservers() = Unit

	override fun initView() {
		setupAdapter()

		binding.introParentLeftBtn.setOnClickListener {
			navigateNext()
		}
		binding.introParentRightBtn.setOnClickListener {
			if (binding.introViewpager.currentItem == (adapter?.itemCount ?: 0) - 1) {
				navigateNext()
			} else {
				binding.introViewpager.setCurrentItem(binding.introViewpager.currentItem + 1, true)
			}
		}

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top, bottom = insets.bottom)
		}
	}

	private fun setupAdapter() {
		adapter = IntroPagerAdapter(requireActivity())
		binding.introViewpager.adapter = adapter
		binding.introViewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
			override fun onPageSelected(position: Int) {
				binding.segmentBar.setProgress(position + 1)

				super.onPageSelected(position)
			}
		})
	}

	private fun navigateNext() {
		findNavController().safeNavigateWithTransition(
			IntroParentFragmentDirections.proceedToOnboarding()
		)
	}
}
