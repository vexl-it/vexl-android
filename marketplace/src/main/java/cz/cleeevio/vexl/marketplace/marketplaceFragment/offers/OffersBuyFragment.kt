package cz.cleeevio.vexl.marketplace.marketplaceFragment.offers

import cz.cleevio.core.utils.repeatScopeOnStart

class OffersBuyFragment constructor(
	navigateToFilters: () -> Unit,
	navigateToNewOffer: () -> Unit,
	requestOffer: (String) -> Unit
) : OffersBaseFragment(navigateToFilters, navigateToNewOffer, requestOffer) {

	override fun bindObservers() {
		super.bindObservers()
		repeatScopeOnStart {
			viewModel.buyOffers.collect { offers ->
				adapter.submitList(offers)
			}
		}
	}

}