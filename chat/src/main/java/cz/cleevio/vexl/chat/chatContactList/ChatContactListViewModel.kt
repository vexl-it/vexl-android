package cz.cleevio.vexl.chat.chatContactList

import androidx.lifecycle.viewModelScope
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.user.User
import cz.cleevio.repository.repository.chat.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class ChatContactListViewModel constructor(
	private val chatRepository: ChatRepository
) : BaseViewModel() {

	private val _usersChattedWith = MutableSharedFlow<List<User>>(replay = 1)
	val usersChattedWith = _usersChattedWith.asSharedFlow()

	init {
		viewModelScope.launch(Dispatchers.IO) {
			val response = chatRepository.loadChatUsers()
			when (response.status) {
				is Status.Success -> {
					response.data?.let { data ->
						_usersChattedWith.emit(
							data
						)
					}
				}
			}
		}
	}

}