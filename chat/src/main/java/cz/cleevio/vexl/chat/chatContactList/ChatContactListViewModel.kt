package cz.cleevio.vexl.chat.chatContactList

import androidx.lifecycle.viewModelScope
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.user.User
import cz.cleevio.repository.repository.chat.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class ChatContactListViewModel constructor(
	private val chatRepository: ChatRepository
) : BaseViewModel() {

	private var usersChattedWithList: List<User> = emptyList()
	private var currentFilter = FilterType.ALL

	private val _usersChattedWith = MutableStateFlow<List<User>>(emptyList())
	val usersChattedWith = _usersChattedWith.asStateFlow()

	init {
		viewModelScope.launch(Dispatchers.IO) {
			val response = chatRepository.loadChatUsers()
			when (response.status) {
				is Status.Success -> {
					response.data?.let { data ->
						usersChattedWithList = data
						emitUsers()
					}
				}
			}
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