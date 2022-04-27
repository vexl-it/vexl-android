package cz.cleeevio.vexl.marketplace.marketplaceFragment.offers

class OffersBuyFragment constructor(
	navigateToFilters: () -> Unit,
	navigateToNewOffer: () -> Unit,
	requestOffer: (String) -> Unit
) : OffersBaseFragment(navigateToFilters, navigateToNewOffer, requestOffer)