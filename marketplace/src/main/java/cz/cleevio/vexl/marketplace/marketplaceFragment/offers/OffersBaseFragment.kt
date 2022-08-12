package cz.cleevio.vexl.marketplace.marketplaceFragment.offers

import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import cz.cleevio.core.model.OfferType
import cz.cleevio.core.utils.ChipViewUtils
import cz.cleevio.core.utils.repeatScopeOnResume
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
	protected val adapter: OffersAdapter by lazy {
		OffersAdapter(requestOffer)
	}

	override fun bindObservers() {
		repeatScopeOnResume {
			viewModel.getFilters(getOfferType()).collect { isFilterInUse ->
				binding.filters.removeAllViews()
				binding.filters.addView(
					ChipViewUtils.generateChipView(
						context = requireContext(),
						icon = R.drawable.ic_chevron_down,
						iconAtStart = false,
						activeState = isFilterInUse,
						filter = getString(R.string.filter_offers),
						listener = {
							navigateToFilters(getOfferType())
						}
					)
				)
			}
		}
		repeatScopeOnResume {
			viewModel.myOffersCount.collect { myOffersCount ->
				processMyOffersButtons(myOffersCount > 0)
			}
		}
	}

	override fun initView() {
		listenForInsets(binding.offersContainer) { insets ->
			// 2x because of once per size of the inset, and twice for the inset of the bottom menu
			binding.offerList.updatePadding(bottom = 2 * insets.bottom)
		}

		binding.offerList.adapter = adapter

		binding.addOfferBtn.setOnClickListener {
			navigateToNewOffer(getOfferType())
		}
		binding.myOffersBtn.setOnClickListener {
			navigateToMyOffers(getOfferType())
		}

		checkMyOffersCount(getOfferType())
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