package cz.cleevio.vexl.marketplace.marketplaceFragment.offers

import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import cz.cleevio.core.model.OfferType
import cz.cleevio.core.utils.ChipViewUtils
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import cz.cleevio.vexl.marketplace.R
import cz.cleevio.vexl.marketplace.databinding.FragmentOffersBinding
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
						ChipViewUtils.generateChipView(
							context = requireContext(),
							filter.label,
							filter.icon
						)
					)
				}
				binding.filters.addView(
					ChipViewUtils.generateChipView(
						context = requireContext(),
						icon = R.drawable.ic_chevron_down,
						iconAtStart = false,
						filter = getString(R.string.filter_offers),
						listener = {
							navigateToFilters(getOfferType())
						}
					)
				)
			}
		}
		repeatScopeOnStart {
			viewModel.myOffersCount.collect { myOffersCount ->
				processMyOffersButtons(myOffersCount > 0)
			}
		}
	}

	override fun initView() {
		listenForInsets(binding.offersContainer) { insets ->
			binding.offerList.updatePadding(bottom = 2 * insets.bottom)    // 2x because of once per size of the inset, and twice for the inset of the bottom menu
		}

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
}