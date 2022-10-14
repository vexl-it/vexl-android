package cz.cleevio.vexl.marketplace.myOffersFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.core.model.OfferType
import cz.cleevio.repository.model.offer.OfferWithGroup
import cz.cleevio.repository.repository.group.GroupRepository
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
	val groupRepository: GroupRepository
) : BaseViewModel() {

	private val _offers = MutableSharedFlow<List<OfferWithGroup>>(replay = 1)
	val offers = _offers.asSharedFlow()

	private val _offersCount = MutableSharedFlow<Int>(replay = 1)
	val offersCount = _offersCount.asSharedFlow()

	fun loadData() {
		viewModelScope.launch(Dispatchers.IO) {
			offerRepository.getOffersSortedByDateOfCreationFlow(offerType.name)
				.map { list ->
						list.map {
							OfferWithGroup(offer = it, group = groupRepository.findGroupByUuidInDB(it.groupUuids.firstOrNull() ?: ""))
						}
				}
				.collect {
					_offers.emit(it)
				}
		}

		viewModelScope.launch(Dispatchers.IO) {
			_offersCount.emit(
				offerRepository.getMyActiveOffersCount(offerType.name)
			)
		}
	}
}