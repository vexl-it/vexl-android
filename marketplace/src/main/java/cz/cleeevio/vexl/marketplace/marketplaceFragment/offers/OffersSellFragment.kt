package cz.cleeevio.vexl.marketplace.marketplaceFragment.offers

import cz.cleevio.core.utils.repeatScopeOnStart


class OffersSellFragment constructor(
	navigateToFilters: () -> Unit,
	navigateToNewOffer: () -> Unit,
	requestOffer: (Long) -> Unit
) : OffersBaseFragment(navigateToFilters, navigateToNewOffer, requestOffer) {

	override fun bindObservers() {
		super.bindObservers()
		repeatScopeOnStart {
			viewModel.sellOffers.collect { offers ->
				adapter.submitList(offers)
			}
		}
	}
}