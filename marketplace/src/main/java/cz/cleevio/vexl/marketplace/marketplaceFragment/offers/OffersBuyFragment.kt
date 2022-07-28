package cz.cleevio.vexl.marketplace.marketplaceFragment.offers

import cz.cleevio.core.model.OfferType
import cz.cleevio.core.utils.repeatScopeOnStart

class OffersBuyFragment constructor(
	navigateToFilters: (OfferType) -> Unit,
	navigateToNewOffer: (OfferType) -> Unit,
	navigateToMyOffers: (OfferType) -> Unit,
	requestOffer: (String) -> Unit
) : OffersBaseFragment(navigateToFilters, navigateToNewOffer, navigateToMyOffers, requestOffer) {

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