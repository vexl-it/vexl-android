package cz.cleevio.vexl.marketplace.marketplaceFragment.offers

import cz.cleevio.core.model.OfferType
import cz.cleevio.core.utils.repeatScopeOnResume

class OffersSellFragment constructor(
	navigateToFilters: (OfferType) -> Unit,
	navigateToNewOffer: (OfferType) -> Unit,
	navigateToMyOffers: (OfferType) -> Unit,
	requestOffer: (String) -> Unit
) : OffersBaseFragment(
	navigateToFilters, navigateToNewOffer = navigateToNewOffer,
	navigateToMyOffers = navigateToMyOffers, requestOffer = requestOffer
) {

	override fun getOfferType(): OfferType = OfferType.SELL

	override fun bindObservers() {
		super.bindObservers()
		repeatScopeOnResume {
			viewModel.sellOffersFlow.collect { offers ->
				adapter.submitList(offers)
			}
		}
	}
}