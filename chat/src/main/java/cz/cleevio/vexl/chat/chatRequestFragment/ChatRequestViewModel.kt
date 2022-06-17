package cz.cleevio.vexl.chat.chatRequestFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.network.data.Resource
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.chat.ChatMessage
import cz.cleevio.repository.model.chat.CommunicationRequest
import cz.cleevio.repository.model.chat.MessageType
import cz.cleevio.repository.repository.chat.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel
import java.util.*

class ChatRequestViewModel constructor(
	private val chatRepository: ChatRepository,
) : BaseViewModel() {

	private val _usersRequestingChat = MutableSharedFlow<List<CommunicationRequest>>(replay = 1)
	val usersRequestingChat = _usersRequestingChat.asSharedFlow()

	private val _communicationRequestResponse = MutableSharedFlow<Pair<CommunicationRequest, Resource<Unit>>>(replay = 1)
	val communicationRequestResponse = _communicationRequestResponse.asSharedFlow()

	init {
		viewModelScope.launch(Dispatchers.IO) {
			//we need message, offer, something else? User?
			val requests = chatRepository.loadCommunicationRequests()
			_usersRequestingChat.emit(
				requests
			)
		}
	}

	fun acceptCommunicationRequest(communicationRequest: CommunicationRequest) {
		viewModelScope.launch(Dispatchers.IO) {
			val response = chatRepository.confirmCommunicationRequest(
				communicationRequest.offer.offerId,
				communicationRequest.message.senderPublicKey,
				ChatMessage(
					uuid = UUID.randomUUID().toString(),
					inboxPublicKey = communicationRequest.message.senderPublicKey,
					senderPublicKey = communicationRequest.offer.offerPublicKey,
					type = MessageType.COMMUNICATION_REQUEST_RESPONSE,
					recipientPublicKey = communicationRequest.message.senderPublicKey
				),
				true
			)
			if (response.status == Status.Success) {
				chatRepository.deleteMessage(communicationRequest)
			}
			_communicationRequestResponse.emit(
				Pair(communicationRequest, response)
			)
		}
	}

}