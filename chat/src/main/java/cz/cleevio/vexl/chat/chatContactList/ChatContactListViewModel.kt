package cz.cleevio.vexl.chat.chatContactList

import androidx.lifecycle.viewModelScope
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import cz.cleevio.core.model.OfferType
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.repository.model.chat.ChatListUser
import cz.cleevio.repository.model.chat.CommunicationRequest
import cz.cleevio.repository.repository.chat.ChatRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatContactListViewModel constructor(
	private val chatRepository: ChatRepository,
	val remoteConfig: FirebaseRemoteConfig,
	val navMainGraphModel: NavMainGraphModel
) : BaseViewModel() {

	private val _usersRequestingChat = MutableSharedFlow<List<CommunicationRequest>>(replay = 1)
	val usersRequestingChat = _usersRequestingChat.asSharedFlow()

	private var usersChattedWithList: List<ChatListUser> = emptyList()
	private var currentFilter = FilterType.ALL

	private val _usersChattedWith = MutableStateFlow<List<ChatListUser>>(emptyList())
	val usersChattedWith = _usersChattedWith.asStateFlow()

	private val _showRefreshIndicator = MutableStateFlow(false)
	val showRefreshIndicator = _showRefreshIndicator.asStateFlow()

	init {
		viewModelScope.launch(Dispatchers.IO) {
			chatRepository.chatUsers.collect {
				usersChattedWithList = it
				emitUsers()
				_showRefreshIndicator.emit(false)
			}
		}
	}

	fun refreshChats() {
		viewModelScope.launch(Dispatchers.IO) {
			_showRefreshIndicator.emit(true)
			chatRepository.startEmittingChatUsers()
		}
	}

	fun refreshChatRequests() {
		viewModelScope.launch(Dispatchers.IO) {
			//we need message, offer, something else? User?
			val requests = chatRepository.loadCommunicationRequests()
			_usersRequestingChat.emit(
				requests
			)
		}
	}

	private suspend fun emitUsers() {
		_usersChattedWith.emit(
			usersChattedWithList.filter { user ->
				val isBuyers = (user.offer.offerType == OfferType.BUY.name && !user.offer.isMine)
					|| (user.offer.offerType == OfferType.SELL.name && user.offer.isMine)
				when (currentFilter) {
					FilterType.ALL -> true
					FilterType.BUYERS -> isBuyers
					FilterType.SELLERS -> !isBuyers
				}
			}.sortedByDescending {
				it.message.time
			}
		)
	}

	fun filter(filterType: FilterType) {
		viewModelScope.launch(Dispatchers.IO) {
			currentFilter = filterType
			emitUsers()
		}
	}

	fun goToMyOfferList(offerType: OfferType) {
		viewModelScope.launch(Dispatchers.IO) {
			navMainGraphModel.navigateToGraph(
				NavMainGraphModel.NavGraph.MyOfferList(offerType = offerType)
			)
		}
	}

	enum class FilterType {
		ALL, BUYERS, SELLERS
	}

}