package cz.cleevio.vexl.chat.chatFragment

import androidx.lifecycle.viewModelScope
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

class ChatViewModel constructor(
	private val chatRepository: ChatRepository,
	val communicationRequest: CommunicationRequest
) : BaseViewModel() {

	val _messageSentSuccessfully = MutableSharedFlow<Boolean>(replay = 1)
	val messageSentSuccessfully = _messageSentSuccessfully.asSharedFlow()

	val messages = communicationRequest.message.let { message ->
		chatRepository.getMessages(
			inboxPublicKey = message.inboxPublicKey,
			senderPublicKeys = listOf(
				message.senderPublicKey,
				message.recipientPublicKey
			)
		)
	}

	fun sendMessage(message: String) {

		viewModelScope.launch(Dispatchers.IO) {

			val myInboxKeys = chatRepository.getMyInboxKeys()
			var senderPublicKey = ""
			var receiverPublicKey = ""
			if (myInboxKeys.contains(communicationRequest.message.senderPublicKey)) {
				senderPublicKey = communicationRequest.message.senderPublicKey
				receiverPublicKey = communicationRequest.message.recipientPublicKey
			} else {
				senderPublicKey = communicationRequest.message.recipientPublicKey
				receiverPublicKey = communicationRequest.message.senderPublicKey
			}

			val messageType = MessageType.TEXT

			val result = chatRepository.sendMessage(
				senderPublicKey = senderPublicKey,
				receiverPublicKey = receiverPublicKey,
				message = ChatMessage(
					inboxPublicKey = communicationRequest.message.inboxPublicKey,
					senderPublicKey = senderPublicKey,
					recipientPublicKey = receiverPublicKey,
					text = message,
					type = messageType,
					isMine = true
				),
				messageType = "MESSAGE"
			)

			_messageSentSuccessfully.emit(result.status == Status.Success)
		}
	}

}