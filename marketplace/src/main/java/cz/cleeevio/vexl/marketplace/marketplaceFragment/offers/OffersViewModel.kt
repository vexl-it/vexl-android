package cz.cleeevio.vexl.marketplace.marketplaceFragment.offers

import androidx.lifecycle.viewModelScope
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.repository.model.offer.Filter
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.repository.offer.OfferRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class OffersViewModel(
	private val offerRepository: OfferRepository,
	private val preferences: EncryptedPreferenceRepository
) : BaseViewModel() {

	private val _buyOffers = MutableSharedFlow<List<Offer>>(replay = 1)
	val buyOffers = _buyOffers.asSharedFlow()

	private val _sellOffers = MutableSharedFlow<List<Offer>>(replay = 1)
	val sellOffers = _sellOffers.asSharedFlow()

	private val _filters = MutableSharedFlow<List<Filter>>(replay = 1)
	val filters = _filters.asSharedFlow()

	init {
		viewModelScope.launch(Dispatchers.IO) {
			offerRepository.getOffersFlow().map { list -> list.filter { it.offerType == "BUY" } }.collect {
				_buyOffers.emit(it)
			}
		}

		viewModelScope.launch(Dispatchers.IO) {
			offerRepository.getOffersFlow().map { list -> list.filter { it.offerType == "SELL" } }.collect {
				_sellOffers.emit(it)
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

	fun containsMyOffer(offers: List<Offer>): Boolean {
		val myPublicKey = preferences.userPublicKey
		return offers.any { offer ->
			offer.userPublicKey == myPublicKey
		}
	}
}