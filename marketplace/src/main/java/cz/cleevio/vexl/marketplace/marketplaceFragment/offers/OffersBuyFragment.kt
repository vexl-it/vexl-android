package cz.cleevio.vexl.marketplace.marketplaceFragment.offers

import cz.cleevio.core.model.OfferType
import cz.cleevio.core.utils.repeatScopeOnResume

class OffersBuyFragment constructor(
	navigateToFilters: (OfferType) -> Unit,
	navigateToNewOffer: (OfferType) -> Unit,
	navigateToMyOffers: (OfferType) -> Unit,
	requestOffer: (String) -> Unit
) : OffersBaseFragment(navigateToFilters, navigateToNewOffer, navigateToMyOffers, requestOffer) {

	override fun getOfferType(): OfferType = OfferType.BUY

	override fun bindObservers() {
		super.bindObservers()
		repeatScopeOnResume {
			viewModel.buyOffers.collect { offers ->
				adapter.submitList(offers)
			}
		}
	}

}