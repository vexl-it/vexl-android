package cz.cleevio.vexl.marketplace.marketplaceFragment.offers

import cz.cleevio.core.model.OfferType
import cz.cleevio.core.utils.repeatScopeOnResume

class OffersSellFragment : OffersBaseFragment() {

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