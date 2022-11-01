package cz.cleevio.vexl.chat.chatRequestFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.network.data.Resource
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.chat.ChatMessage
import cz.cleevio.repository.model.chat.CommunicationRequest
import cz.cleevio.repository.model.chat.MessageType
import cz.cleevio.repository.repository.chat.ChatRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.*

class ChatRequestViewModel constructor(
	private val chatRepository: ChatRepository,
) : BaseViewModel() {

	private val _usersRequestingChat = MutableSharedFlow<List<CommunicationRequest>>(replay = 1)
	val usersRequestingChat = _usersRequestingChat.asSharedFlow()

	private val _communicationRequestResponse = MutableSharedFlow<Pair<CommunicationRequest, Resource<Any>>>(replay = 1)
	val communicationRequestResponse = _communicationRequestResponse.asSharedFlow()

	init {
		reloadCommunicationRequests()
	}

	fun reloadCommunicationRequests() {
		viewModelScope.launch(Dispatchers.IO) {
			loadCommunicationRequests()
		}
	}

	private suspend fun loadCommunicationRequests() {
		//we need message, offer, something else? User?
		val requests = chatRepository.loadCommunicationRequests()
		_usersRequestingChat.emit(
			requests
		)
	}

	fun processCommunicationRequest(
		communicationRequest: CommunicationRequest,
		approve: Boolean,
		text: String = ""
	) {
		viewModelScope.launch(Dispatchers.IO) {
			val response = chatRepository.confirmCommunicationRequest(
				offerId = communicationRequest.offer.offerId,
				publicKeyToConfirm = communicationRequest.message.senderPublicKey,
				message = ChatMessage(
					uuid = UUID.randomUUID().toString(),
					inboxPublicKey = communicationRequest.message.inboxPublicKey,
					senderPublicKey = communicationRequest.offer.offerPublicKey,
					text = text,
					type = if (approve) MessageType.APPROVE_MESSAGING else MessageType.DISAPPROVE_MESSAGING,
					recipientPublicKey = communicationRequest.message.senderPublicKey,
					isMine = true,
					isProcessed = false
				),
				originalRequestMessage = communicationRequest.message,
				approve = approve
			)
			if (response.status == Status.Success) {
				chatRepository.deleteMessage(communicationRequest)
			}

			if (approve) {
				_communicationRequestResponse.emit(
					Pair(communicationRequest, response as Resource<Any>)
				)
			} else {
				//reload data, old message should be processed
				loadCommunicationRequests()
			}
		}
	}

}