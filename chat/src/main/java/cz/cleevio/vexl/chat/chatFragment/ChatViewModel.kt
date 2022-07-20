package cz.cleevio.vexl.chat.chatFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.chat.ChatMessage
import cz.cleevio.repository.model.chat.ChatUser
import cz.cleevio.repository.model.chat.CommunicationRequest
import cz.cleevio.repository.model.chat.MessageType
import cz.cleevio.repository.repository.chat.ChatRepository
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import lightbase.core.baseClasses.BaseViewModel

class ChatViewModel constructor(
	private val chatRepository: ChatRepository,
	private val userRepository: UserRepository,
	val communicationRequest: CommunicationRequest
) : BaseViewModel() {

	protected var messagePullJob: Job? = null

	val _messageSentSuccessfully = MutableSharedFlow<Boolean>(replay = 1)
	val messageSentSuccessfully = _messageSentSuccessfully.asSharedFlow()

	val messages = communicationRequest.message.let { message ->
		chatRepository.getMessages(
			inboxPublicKey = message.inboxPublicKey,
			firstKey = message.senderPublicKey,
			secondKey = message.recipientPublicKey
		)
	}

	val hasPendingIdentityRevealRequests = communicationRequest.message.let { message ->
		chatRepository.getPendingIdentityRequest(
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

			startMessageRefresh(myInboxPublicKey = senderPublicKey)
		}
	}

	fun sendMessage(message: String) {

		viewModelScope.launch(Dispatchers.IO) {
			val messageType = MessageType.MESSAGE

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
				messageType = messageType.name
			)

			_messageSentSuccessfully.emit(result.status == Status.Success)
		}
	}

	fun resolveIdentityRevealRequest(approved: Boolean) {
		viewModelScope.launch(Dispatchers.IO) {
			val messageType = if (approved) MessageType.APPROVE_REVEAL else MessageType.DISAPPROVE_REVEAL

			val user = userRepository.getUser()?.let {
				ChatUser(
					name = it.username,
					image = it.avatar
				)
			}

			val response = chatRepository.sendMessage(
				senderPublicKey = senderPublicKey,
				receiverPublicKey = receiverPublicKey,
				message = ChatMessage(
					inboxPublicKey = communicationRequest.message.inboxPublicKey,
					senderPublicKey = senderPublicKey,
					recipientPublicKey = receiverPublicKey,
					type = messageType,
					deanonymizedUser = user,
					isMine = true,
					isProcessed = false
				),
				messageType = messageType.name
			)

			when (response.status) {
				is Status.Success -> {
					chatRepository.solveIdentityRevealRequest(
						inboxPublicKey = communicationRequest.message.inboxPublicKey,
						firstKey = senderPublicKey,
						secondKey = receiverPublicKey
					)
				}
			}
		}
	}

	private fun startMessageRefresh(myInboxPublicKey: String) {
		messagePullJob = viewModelScope.launch(Dispatchers.IO) {
			while (isActive) {
				chatRepository.syncMessages(myInboxPublicKey)
				delay(MESSAGE_PULL_TIMEOUT)
			}
		}
	}

	companion object {
		const val MESSAGE_PULL_TIMEOUT = 20_000L
	}
}