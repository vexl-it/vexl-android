package cz.cleevio.vexl.chat.chatRequestFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.user.User
import cz.cleevio.repository.repository.chat.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class ChatRequestViewModel constructor(
	private val chatRepository: ChatRepository,
) : BaseViewModel() {

	private val _usersRequestingChat = MutableSharedFlow<List<User>>(replay = 1)
	val usersRequestingChat = _usersRequestingChat.asSharedFlow()

	init {
		viewModelScope.launch(Dispatchers.IO) {
			//todo: we need request, message, offer, something else?
			val response = chatRepository.loadChatRequests()
			when (response.status) {
				is Status.Success -> {
					response.data?.let { data ->
						_usersRequestingChat.emit(
							data
						)
					}
				}
			}
		}
	}
}