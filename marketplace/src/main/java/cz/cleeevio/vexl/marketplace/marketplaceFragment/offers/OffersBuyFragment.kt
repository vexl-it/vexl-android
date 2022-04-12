package cz.cleeevio.vexl.marketplace.marketplaceFragment.offers

class OffersBuyFragment(
	navigateToFilters: () -> Unit,
	navigateToNewOffer: () -> Unit,
	requestOffer: (Long) -> Unit
) : OffersBaseFragment(navigateToFilters, navigateToNewOffer, requestOffer)