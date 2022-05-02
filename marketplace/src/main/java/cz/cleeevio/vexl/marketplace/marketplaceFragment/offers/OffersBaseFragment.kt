package cz.cleeevio.vexl.marketplace.marketplaceFragment.offers

import android.view.View
import androidx.annotation.DrawableRes
import com.google.android.material.chip.Chip
import cz.cleeevio.vexl.marketplace.R
import cz.cleeevio.vexl.marketplace.databinding.FragmentOffersBinding
import cz.cleevio.core.utils.getDrawable
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import lightbase.core.baseClasses.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

sealed class OffersBaseFragment constructor(
	val navigateToFilters: () -> Unit,
	val navigateToNewOffer: () -> Unit,
	val requestOffer: (String) -> Unit
) : BaseFragment(R.layout.fragment_offers) {

	override val viewModel by viewModel<OffersViewModel>()
	private val binding by viewBinding(FragmentOffersBinding::bind)
	lateinit var adapter: OffersAdapter

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.filters.collect { filters ->
				binding.filters.removeAllViews()
				filters.forEach { filter ->
					binding.filters.addView(
						generateChipView(
							filter.label,
							filter.icon
						)
					)
				}
				binding.filters.addView(generateChipView(
					icon = R.drawable.ic_chevron_down,
					listener = {
						navigateToFilters()
					}
				))
			}
		}
	}

	override fun initView() {
		adapter = OffersAdapter(requestOffer)
		binding.offerList.adapter = adapter

		binding.addOfferBtn.setOnClickListener {
			navigateToNewOffer()
		}

		viewModel.getFilters()
	}


	private fun generateChipView(filter: String? = null, @DrawableRes icon: Int? = null, listener: () -> (Unit) = {}): Chip {
		val newChip = Chip(context)
		newChip.id = View.generateViewId()
		newChip.setEnsureMinTouchTargetSize(false)
		newChip.setChipBackgroundColorResource(R.color.gray_1)
		newChip.setRippleColorResource(R.color.gray_2)
		newChip.setOnClickListener {
			listener()
		}

		filter?.let {
			newChip.setTextAppearance(R.style.TextAppearance_Vexl_Marketplace_FilterChip)
			newChip.text = filter
		} ?: run {
			newChip.textStartPadding = 0.0f
			newChip.textEndPadding = 0.0f
		}
		icon?.let {
			newChip.chipIcon = getDrawable(icon)
		}
		return newChip
	}

}