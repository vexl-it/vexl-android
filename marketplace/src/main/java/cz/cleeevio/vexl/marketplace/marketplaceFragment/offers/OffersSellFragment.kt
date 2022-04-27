package cz.cleeevio.vexl.marketplace.marketplaceFragment.offers


class OffersSellFragment(
	navigateToFilters: () -> Unit,
	navigateToNewOffer: () -> Unit,
	requestOffer: (String) -> Unit
) : OffersBaseFragment(navigateToFilters, navigateToNewOffer, requestOffer)