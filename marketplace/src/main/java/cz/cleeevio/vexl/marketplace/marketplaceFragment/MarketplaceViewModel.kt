package cz.cleeevio.vexl.marketplace.marketplaceFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.repository.repository.offer.OfferRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class MarketplaceViewModel constructor(
	val navMainGraphModel: NavMainGraphModel,
	val offerRepository: OfferRepository
) : BaseViewModel() {

	fun syncOffers() {
		viewModelScope.launch(Dispatchers.IO) {
			offerRepository.syncOffers()
		}
	}

}