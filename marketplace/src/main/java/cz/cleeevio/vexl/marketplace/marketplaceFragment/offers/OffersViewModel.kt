package cz.cleeevio.vexl.marketplace.marketplaceFragment.offers

import androidx.lifecycle.viewModelScope
import cz.cleeevio.vexl.marketplace.R
import cz.cleevio.repository.model.offer.Filter
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.repository.offer.OfferRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel
import java.time.ZonedDateTime

class OffersViewModel(
	private val offerRepository: OfferRepository
) : BaseViewModel() {

	private val _buyOffers = MutableSharedFlow<List<Offer>>(replay = 1)
	val buyOffers = _buyOffers.asSharedFlow()

	private val _filters = MutableSharedFlow<List<Filter>>(replay = 1)
	val filters = _filters.asSharedFlow()

	fun getFilters() {
		viewModelScope.launch(Dispatchers.IO) {
			_filters.emit(
				listOf(
					Filter(
						label = "Revolut",
						icon = R.drawable.ic_revolut
					),
					Filter(
						label = "up to \$10k"
					)
				)
			)
		}
	}

	fun getData() {
		viewModelScope.launch(Dispatchers.IO) {
//			val response = offerRepository.loadOffersForMe()
//			when (response.status) {
//				is Status.Success -> response.data?.let { data ->
//					_buyOffers.emit(data)
//				}
//			}

			// TODO vyhodit
			_buyOffers.emit(
				listOf(
					Offer(
						offerId = 1L,
						location = "Prague 7",
						userPublicKey = "pub key 1",
						offerPublicKey = "",
						direction = "I’ll be wearing a red hat, Don’t text me before 9am — I love to sleep...",
						offerSymKey = "",
						amount = "100",
						fee = "Wants \$30 fee per transaction",
						friendLevel = "",
						onlyInPerson = "",
						paymentMethod = "Cash",
						typeNetwork = "",
						createdAt = ZonedDateTime.now(),
						modifiedAt = ZonedDateTime.now()
					),
					Offer(
						offerId = 2L,
						location = "Prague 8",
						userPublicKey = "pub key 2",
						offerPublicKey = "",
						direction = "I’ll be wearing a red hat, Don’t text me before 9am — I love to sleep...",
						offerSymKey = "",
						amount = "1000",
						fee = "Wants \$10 fee per transaction",
						friendLevel = "",
						onlyInPerson = "",
						paymentMethod = "DogeCoin",
						typeNetwork = "",
						createdAt = ZonedDateTime.now(),
						modifiedAt = ZonedDateTime.now()
					),
					Offer(
						offerId = 3L,
						location = "Prague 9",
						userPublicKey = "pub key 3",
						offerPublicKey = "",
						direction = "I’ll be wearing a red hat, Don’t text me before 9am — I love to sleep...",
						offerSymKey = "",
						amount = "9999",
						fee = null,
						friendLevel = "",
						onlyInPerson = "",
						paymentMethod = "Revolut",
						typeNetwork = "",
						createdAt = ZonedDateTime.now(),
						modifiedAt = ZonedDateTime.now()
					),
				)
			)
		}
	}

}