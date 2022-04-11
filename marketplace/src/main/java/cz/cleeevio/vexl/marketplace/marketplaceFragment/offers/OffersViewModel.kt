package cz.cleeevio.vexl.marketplace.marketplaceFragment.offers

import androidx.lifecycle.viewModelScope
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.repository.offer.OfferRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class OffersViewModel(
	private val offerRepository: OfferRepository
) : BaseViewModel() {

	private val _buyOffers = MutableSharedFlow<List<Offer>>(replay = 1)
	val buyOffers = _buyOffers.asSharedFlow()

	fun getData() {
		viewModelScope.launch(Dispatchers.IO) {
			_buyOffers.emit(
				listOf(
					Offer(
						id = 1,
						location = "",
						userPublicKey = "",
						offerPublicKey = "",
						direction = "",
						premium = "",
						threshold = "",
						offerSymKey = ""
					),
					Offer(
						id = 2,
						location = "",
						userPublicKey = "",
						offerPublicKey = "",
						direction = "",
						premium = "",
						threshold = "",
						offerSymKey = ""
					),
					Offer(
						id = 3,
						location = "",
						userPublicKey = "",
						offerPublicKey = "",
						direction = "",
						premium = "",
						threshold = "",
						offerSymKey = ""
					),
				)
			)
		}
	}

}