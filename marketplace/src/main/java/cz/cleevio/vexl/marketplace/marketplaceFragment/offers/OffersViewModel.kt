package cz.cleevio.vexl.marketplace.marketplaceFragment.offers

import androidx.lifecycle.viewModelScope
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import cz.cleevio.core.model.OfferType
import cz.cleevio.repository.model.offer.OfferWithGroup
import cz.cleevio.repository.repository.group.GroupRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import cz.cleevio.vexl.marketplace.marketplaceFragment.NavMarketplaceGraphModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OffersViewModel constructor(
	val groupRepository: GroupRepository,
	private val navMarketplaceGraphModel: NavMarketplaceGraphModel,
	private val offerRepository: OfferRepository,
	val remoteConfig: FirebaseRemoteConfig
) : BaseViewModel() {

	val buyOffersFlow = offerRepository.buyOfferFilter.flatMapLatest { filter ->
		combine(
			offerRepository.getFilteredAndSortedOffersByTypeFlow(OfferType.BUY.name, filter),
			groupRepository.getGroupsFlow()
		) { offers, groups ->
			offers.map { item ->
				OfferWithGroup(offer = item, group = groups.firstOrNull { it.groupUuid == item.groupUuid })
			}.sortedBy {
				if (it.offer.isRequested) return@sortedBy 1

				return@sortedBy 0
			}
		}
	}.flowOn(Dispatchers.Default)

	val sellOffersFlow = offerRepository.sellOfferFilter.flatMapLatest { filter ->
		combine(
			offerRepository.getFilteredAndSortedOffersByTypeFlow(OfferType.SELL.name, filter),
			groupRepository.getGroupsFlow()
		) { offers, groups ->
			offers.map { item ->
				OfferWithGroup(offer = item, group = groups.firstOrNull { it.groupUuid == item.groupUuid })
			}.sortedBy {
				if (it.offer.isRequested) return@sortedBy 1

				return@sortedBy 0
			}
		}
	}.flowOn(Dispatchers.Default)

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

	fun navigateToGraph(navGraph: NavMarketplaceGraphModel.NavGraph) {
		viewModelScope.launch(Dispatchers.Default) {
			navMarketplaceGraphModel.navigateToGraph(navGraph)
		}
	}
}