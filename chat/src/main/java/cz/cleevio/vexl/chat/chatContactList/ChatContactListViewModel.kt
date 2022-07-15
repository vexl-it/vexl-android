package cz.cleevio.vexl.chat.chatContactList

import androidx.lifecycle.viewModelScope
import cz.cleevio.repository.model.chat.ChatListUser
import cz.cleevio.repository.model.chat.CommunicationRequest
import cz.cleevio.repository.repository.chat.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class ChatContactListViewModel constructor(
	private val chatRepository: ChatRepository
) : BaseViewModel() {

	private val _usersRequestingChat = MutableSharedFlow<List<CommunicationRequest>>(replay = 1)
	val usersRequestingChat = _usersRequestingChat.asSharedFlow()

	private var usersChattedWithList: List<ChatListUser> = emptyList()
	private var currentFilter = FilterType.ALL

	private val _usersChattedWith = MutableStateFlow<List<ChatListUser>>(emptyList())
	val usersChattedWith = _usersChattedWith.asStateFlow()

	private val _showRefreshIndicator = MutableStateFlow<Boolean>(false)
	val showRefreshIndicator = _showRefreshIndicator.asStateFlow()

	fun refreshChats() {
		viewModelScope.launch(Dispatchers.IO) {
			_showRefreshIndicator.emit(true)
			val data = chatRepository.loadChatUsers()
			usersChattedWithList = data
			emitUsers()
			_showRefreshIndicator.emit(false)
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
				when (currentFilter) {
					FilterType.ALL -> true
					FilterType.BUYERS -> false // TODO find out how to do this
					FilterType.SELLERS -> false // TODO find out how to do this
				}
			}
		)
	}

	fun filter(filterType: FilterType) {
		viewModelScope.launch(Dispatchers.IO) {
			currentFilter = filterType
			emitUsers()
		}
	}

	enum class FilterType {
		ALL, BUYERS, SELLERS
	}

}