package cz.cleevio.vexl.marketplace.marketplaceFragment.offers

import cz.cleevio.core.RemoteConfigConstants
import cz.cleevio.core.model.OfferType
import cz.cleevio.core.utils.repeatScopeOnResume

class OffersBuyFragment : OffersBaseFragment() {

	override fun getOfferType(): OfferType = OfferType.BUY

	override fun bindObservers() {
		super.bindObservers()
		repeatScopeOnResume {
			viewModel.buyOffersFlow.collect { offers ->
				changeLockedVisibility(viewModel.remoteConfig.getBoolean(RemoteConfigConstants.MARKETPLACE_LOCKED))
				adapter.submitList(offers)
			}
		}
	}
}