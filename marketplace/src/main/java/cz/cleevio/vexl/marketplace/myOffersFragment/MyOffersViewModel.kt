package cz.cleevio.vexl.marketplace.myOffersFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.core.model.OfferType
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.repository.offer.OfferRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MyOffersViewModel constructor(
	private val offerType: OfferType,
	private val offerRepository: OfferRepository,
) : BaseViewModel() {

	private val _offers = MutableSharedFlow<List<Offer>>(replay = 1)
	val offers = _offers.asSharedFlow()

	private val _offersCount = MutableSharedFlow<Int>(replay = 1)
	val offersCount = _offersCount.asSharedFlow()

	fun loadData() {
		viewModelScope.launch(Dispatchers.IO) {
			offerRepository.getOffersFlow().map { list ->
				list.filter { it.offerType == offerType.name && it.isMine }
			}.collect {
				_offers.emit(it)
			}
		}

		viewModelScope.launch(Dispatchers.IO) {
			_offersCount.emit(offerRepository.getMyOffersCount(offerType.name))
		}
	}
}