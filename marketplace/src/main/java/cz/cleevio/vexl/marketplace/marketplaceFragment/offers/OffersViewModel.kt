package cz.cleevio.vexl.marketplace.marketplaceFragment.offers

import androidx.lifecycle.viewModelScope
import cz.cleevio.core.model.OfferType
import cz.cleevio.repository.model.offer.Filter
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.repository.offer.OfferRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class OffersViewModel(
	private val offerRepository: OfferRepository
) : BaseViewModel() {

	private val _buyOffers = MutableSharedFlow<List<Offer>>(replay = 1)
	val buyOffers = _buyOffers.asSharedFlow()

	private val _sellOffers = MutableSharedFlow<List<Offer>>(replay = 1)
	val sellOffers = _sellOffers.asSharedFlow()

	private val _filters = MutableSharedFlow<List<Filter>>(replay = 1)
	val filters = _filters.asSharedFlow()

	private val _myOffersCount = MutableSharedFlow<Int>(replay = 1)
	val myOffersCount = _myOffersCount.asSharedFlow()

	init {
		viewModelScope.launch(Dispatchers.IO) {
			val sortedByDate = offerRepository.getOffersFlow().map { list -> list.filter { it.offerType == OfferType.BUY.name && !it.isMine }.sortedByDescending { it.createdAt } }

			sortedByDate.collect { offers ->
				val notRequestedOffers = offers.filter { !it.isRequested }
				val alreadyRequestedOffers = offers.filter { it.isRequested }
				_buyOffers.emit(notRequestedOffers + alreadyRequestedOffers)
			}
		}

		viewModelScope.launch(Dispatchers.IO) {
			val sortedByDate = offerRepository.getOffersFlow().map { list -> list.filter { it.offerType == OfferType.SELL.name && !it.isMine }.sortedByDescending { it.createdAt } }

			sortedByDate.collect { offers ->
				val notRequestedOffers = offers.filter { !it.isRequested }
				val alreadyRequestedOffers = offers.filter { it.isRequested }
				_sellOffers.emit(notRequestedOffers + alreadyRequestedOffers)
			}
		}
	}

	fun getFilters() {
		viewModelScope.launch(Dispatchers.IO) {
			_filters.emit(
				listOf(
					// TODO emit correct filters

				)
			)
		}
	}

	fun checkMyOffersCount(offerType: OfferType) {
		viewModelScope.launch(Dispatchers.IO) {
			val myOfferCount = offerRepository.getMyOffersCount(offerType.name)
			_myOffersCount.emit(myOfferCount)
		}
	}
}