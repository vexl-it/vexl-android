package cz.cleevio.vexl.marketplace.marketplaceFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.network.data.Status
import cz.cleevio.repository.repository.chat.ChatRepository
import cz.cleevio.repository.repository.group.GroupRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import cz.cleevio.repository.repository.user.UserRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MarketplaceViewModel constructor(
	val navMainGraphModel: NavMainGraphModel,
	val offerRepository: OfferRepository,
	val userRepository: UserRepository,
	val chatRepository: ChatRepository,
	val groupRepository: GroupRepository,
) : BaseViewModel() {

	fun syncOffers() {
		viewModelScope.launch(Dispatchers.IO) {
			offerRepository.syncOffers()
		}
	}

	//just debug to see if avatar link works
	fun loadMe() {
		viewModelScope.launch(Dispatchers.IO) {
			val response = userRepository.getUserMe()
			when (response.status) {
				is Status.Success -> {
					//print link
					Timber.tag("USER").d("${response.data}")
				}
				is Status.Error -> {

				}
			}
		}
	}

	fun syncAllMessages() {
		viewModelScope.launch(Dispatchers.IO) {
			chatRepository.syncAllMessages()
		}
	}

	fun syncMyGroupsData() {
		viewModelScope.launch(Dispatchers.IO) {
			groupRepository.syncMyGroups()
		}
	}
}