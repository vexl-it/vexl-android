package cz.cleeevio.vexl.marketplace.marketplaceFragment.offers

import cz.cleevio.core.model.OfferType
import cz.cleevio.core.utils.repeatScopeOnStart

class OffersBuyFragment constructor(
	navigateToFilters: (OfferType) -> Unit,
	navigateToNewOffer: (OfferType) -> Unit,
	requestOffer: (String) -> Unit
) : OffersBaseFragment(navigateToFilters, navigateToNewOffer, requestOffer) {

	override fun getOfferType(): OfferType = OfferType.BUY

	override fun bindObservers() {
		super.bindObservers()
		repeatScopeOnStart {
			viewModel.buyOffers.collect { offers ->
				adapter.submitList(offers)
			}
		}
	}

}