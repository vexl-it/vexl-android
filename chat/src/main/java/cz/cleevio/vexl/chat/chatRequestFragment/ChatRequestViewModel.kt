package cz.cleevio.vexl.chat.chatRequestFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.repository.model.chat.CommunicationRequest
import cz.cleevio.repository.repository.chat.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class ChatRequestViewModel constructor(
	private val chatRepository: ChatRepository,
) : BaseViewModel() {

	private val _usersRequestingChat = MutableSharedFlow<List<CommunicationRequest>>(replay = 1)
	val usersRequestingChat = _usersRequestingChat.asSharedFlow()

	init {
		viewModelScope.launch(Dispatchers.IO) {
			//we need message, offer, something else? User?
			val requests = chatRepository.loadCommunicationRequests()
			_usersRequestingChat.emit(
				requests
			)
		}
	}
}