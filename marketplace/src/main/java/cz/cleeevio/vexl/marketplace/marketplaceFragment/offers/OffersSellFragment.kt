package cz.cleeevio.vexl.marketplace.marketplaceFragment.offers

import cz.cleevio.core.model.OfferType
import cz.cleevio.core.utils.repeatScopeOnStart


class OffersSellFragment constructor(
	navigateToFilters: (OfferType) -> Unit,
	navigateToNewOffer: (OfferType) -> Unit,
	requestOffer: (String) -> Unit
) : OffersBaseFragment(navigateToFilters, navigateToNewOffer, requestOffer) {

	override fun getOfferType(): OfferType = OfferType.SELL

	override fun bindObservers() {
		super.bindObservers()
		repeatScopeOnStart {
			viewModel.sellOffers.collect { offers ->
				adapter.submitList(offers)
			}
		}
	}
}