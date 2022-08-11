package cz.cleevio.vexl.marketplace.marketplaceFragment.offers

import androidx.lifecycle.viewModelScope
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.model.OfferType
import cz.cleevio.repository.repository.offer.OfferRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OffersViewModel constructor(
	private val offerRepository: OfferRepository
) : BaseViewModel() {

	val buyOffersFlow = offerRepository.buyOfferFilter.flatMapLatest { filter ->
		offerRepository.getFilteredAndSortedOffersByTypeFlow(OfferType.BUY.name, filter)
	}.flowOn(Dispatchers.Default)

	val sellOffersFlow = offerRepository.sellOfferFilter.flatMapLatest { filter ->
		offerRepository.getFilteredAndSortedOffersByTypeFlow(OfferType.SELL.name, filter)
	}.flowOn(Dispatchers.Default)

	/*private val _filters = MutableSharedFlow<List<Filter>>(replay = 1)
	val filters = _filters.asSharedFlow()*/

	private val _myOffersCount = MutableSharedFlow<Int>(replay = 1)
	val myOffersCount = _myOffersCount.asSharedFlow()

	fun getFilters(offerType: OfferType) =
		if (offerType.isBuy()) {
			offerRepository.buyOfferFilter.map { offerFilter ->
				offerFilter.isFilterInUse()
			}
		} else {
			offerRepository.sellOfferFilter.map { offerFilter ->
				offerFilter.isFilterInUse()
			}
		}

	fun checkMyOffersCount(offerType: OfferType) {
		viewModelScope.launch(Dispatchers.IO) {
			val myOfferCount = offerRepository.getMyOffersCount(offerType.name)
			_myOffersCount.emit(myOfferCount)
		}
	}
}