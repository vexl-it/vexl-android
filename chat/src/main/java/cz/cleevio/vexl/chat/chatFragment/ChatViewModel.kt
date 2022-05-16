package cz.cleevio.vexl.chat.chatFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.user.User
import cz.cleevio.repository.repository.chat.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class ChatViewModel constructor(
	private val chatRepository: ChatRepository,
	val user: User
) : BaseViewModel() {

	//todo: change data type
	private val _messages = MutableSharedFlow<List<Any>>(replay = 1)
	val messages = _messages.asSharedFlow()

	init {
		viewModelScope.launch(Dispatchers.IO) {
			val response = chatRepository.loadMessages(user.id)
			when (response.status) {
				is Status.Success -> {
					response.data?.let { data ->
						_messages.emit(
							data
						)
					}
				}
			}
		}
	}

	init {
		//load messages for user
	}
}