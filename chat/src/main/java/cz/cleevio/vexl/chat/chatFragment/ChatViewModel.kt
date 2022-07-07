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
			firstKey = message.senderPublicKey,
			secondKey = message.recipientPublicKey
		)
	}

	//my public key
	lateinit var senderPublicKey: String

	//my friend public key
	lateinit var receiverPublicKey: String

	init {
		viewModelScope.launch(Dispatchers.IO) {
			val myInboxKeys = chatRepository.getMyInboxKeys()

			if (myInboxKeys.contains(communicationRequest.message.senderPublicKey)) {
				senderPublicKey = communicationRequest.message.senderPublicKey
				receiverPublicKey = communicationRequest.message.recipientPublicKey
			} else {
				senderPublicKey = communicationRequest.message.recipientPublicKey
				receiverPublicKey = communicationRequest.message.senderPublicKey
			}
		}
	}

	fun sendMessage(message: String) {

		viewModelScope.launch(Dispatchers.IO) {
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
					isMine = true,
					isProcessed = false
				),
				messageType = "MESSAGE"
			)

			_messageSentSuccessfully.emit(result.status == Status.Success)
		}
	}

}