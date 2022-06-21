package cz.cleeevio.vexl.marketplace.marketplaceFragment.offers

import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import cz.cleeevio.vexl.marketplace.R
import cz.cleeevio.vexl.marketplace.databinding.FragmentOffersBinding
import cz.cleevio.core.model.OfferType
import cz.cleevio.core.utils.getDrawable
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import lightbase.core.baseClasses.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel


sealed class OffersBaseFragment constructor(
	val navigateToFilters: (OfferType) -> Unit,
	val navigateToNewOffer: (OfferType) -> Unit,
	val navigateToMyOffers: (OfferType) -> Unit,
	val requestOffer: (String) -> Unit
) : BaseFragment(R.layout.fragment_offers) {

	abstract fun getOfferType(): OfferType

	override val viewModel by viewModel<OffersViewModel>()
	protected val binding by viewBinding(FragmentOffersBinding::bind)
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
					iconAtStart = false,
					filter = getString(R.string.filter_offers),
					listener = {
						navigateToFilters(getOfferType())
					}
				))
			}
		}
		repeatScopeOnStart {
			viewModel.myOffersCount.collect { myOffersCount ->
				processMyOffersButtons(myOffersCount > 0)
			}
		}
	}

	override fun initView() {
		adapter = OffersAdapter(requestOffer)
		binding.offerList.adapter = adapter

		binding.addOfferBtn.setOnClickListener {
			navigateToNewOffer(getOfferType())
		}
		binding.myOffersBtn.setOnClickListener {
			navigateToMyOffers(getOfferType())
		}

		checkMyOffersCount(getOfferType())

		viewModel.getFilters()
	}

	private fun processMyOffersButtons(hasMyOffers: Boolean) {
		if (hasMyOffers) {
			binding.addOfferBtn.isVisible = false
			binding.myOffersBtn.isVisible = true
		} else {
			binding.addOfferBtn.isVisible = true
			binding.myOffersBtn.isVisible = false
		}
	}

	protected fun checkMyOffersCount(offerType: OfferType) {
		viewModel.checkMyOffersCount(offerType)
	}

	private fun generateChipView(
		filter: String? = null,
		@DrawableRes icon: Int? = null,
		iconAtStart: Boolean = true,
		listener: () -> (Unit) = {}
	): Chip {

		val newChip = Chip(context)
		val chipDrawable = ChipDrawable.createFromAttributes(
			requireContext(),
			null,
			0,
			R.style.Widget_Cleevio_Vexl_Marketplace_FilterChip
		)
		newChip.setChipDrawable(chipDrawable)

		newChip.id = View.generateViewId()
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
			val icon = getDrawable(icon)
			icon?.setTint(resources.getColor(R.color.gray_3, null))
			newChip.chipIcon = icon
			newChip.isChipIconVisible = true
			newChip.layoutDirection = if (iconAtStart) {
				View.LAYOUT_DIRECTION_LTR
			} else {
				View.LAYOUT_DIRECTION_RTL
			}
		}
		return newChip
	}

}