package cz.cleevio.vexl.chat.chatFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.chat.ChatMessage
import cz.cleevio.repository.model.chat.ChatUser
import cz.cleevio.repository.model.chat.CommunicationRequest
import cz.cleevio.repository.model.chat.MessageType
import cz.cleevio.repository.repository.chat.ChatRepository
import cz.cleevio.repository.repository.user.UserRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ChatViewModel constructor(
	private val chatRepository: ChatRepository,
	private val userRepository: UserRepository,
	val communicationRequest: CommunicationRequest
) : BaseViewModel() {

	private val _identityRevealed = MutableSharedFlow<Boolean>(replay = 1)
	val identityRevealed = _identityRevealed.asSharedFlow()

	private val _requestIdentityChannel = Channel<Resource<Unit>>()
	val requestIdentityFlow = _requestIdentityChannel.receiveAsFlow()

	private val _resolveIdentityRevealChannel = Channel<Resource<Unit>>()
	val resolveIdentityRevealFlow = _resolveIdentityRevealChannel.receiveAsFlow()

	val chatUserIdentity = communicationRequest.let { communicationRequest ->
		chatRepository.getChatUserIdentityFlow(
			inboxKey = communicationRequest.message.inboxPublicKey,
			contactPublicKey = if (communicationRequest.message.isMine) {
				communicationRequest.message.recipientPublicKey
			} else {
				communicationRequest.message.senderPublicKey
			}
		)
	}

	val messages = communicationRequest.message.let { message ->
		chatRepository.getMessages(
			inboxPublicKey = message.inboxPublicKey,
			firstKey = message.senderPublicKey,
			secondKey = message.recipientPublicKey
		).map {
			processMessages(it)
		}.flowOn(Dispatchers.Default)
	}

	val hasPendingIdentityRevealRequests = communicationRequest.message.let { message ->
		chatRepository.getPendingIdentityRequest(
			inboxPublicKey = message.inboxPublicKey,
			firstKey = message.senderPublicKey,
			secondKey = message.recipientPublicKey
		)
	}

	val canRequestIdentity = communicationRequest.message.let { message ->
		chatRepository.canRequestIdentity(
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
			chatRepository.sendMessage(
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
		}
	}

	fun requestIdentityReveal() {
		viewModelScope.launch(Dispatchers.IO) {
			_requestIdentityChannel.send(Resource.loading())

			val user = userRepository.getUser()?.let {
				ChatUser(
					name = it.username,
					image = it.avatar,
					imageBase64 = it.avatarBase64
				)
			}
			val messageType = MessageType.REQUEST_REVEAL
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
				messageType = messageType.name,
				storeMessageAlsoWhenFails = false
			)

			_requestIdentityChannel.send(response)
		}
	}

	fun resolveIdentityRevealRequest(approved: Boolean) {
		viewModelScope.launch(Dispatchers.IO) {
			_resolveIdentityRevealChannel.send(Resource.loading())

			val messageType = if (approved) MessageType.APPROVE_REVEAL else MessageType.DISAPPROVE_REVEAL
			val user = userRepository.getUser()?.let {
				ChatUser(
					name = it.username,
					image = it.avatar,
					imageBase64 = it.avatarBase64
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
					isProcessed = true
				),
				messageType = messageType.name,
				storeMessageAlsoWhenFails = false
			)

			_resolveIdentityRevealChannel.send(response)

			if (response.isSuccess()) {
				_identityRevealed.emit(approved)
				if (approved) {
					chatRepository.deAnonymizeUser(
						inboxKey = communicationRequest.message.inboxPublicKey,
						contactPublicKey = receiverPublicKey,
						myPublicKey = senderPublicKey
					)
				}
				chatRepository.solveIdentityRevealRequest(
					inboxPublicKey = communicationRequest.message.inboxPublicKey,
					firstKey = senderPublicKey,
					secondKey = receiverPublicKey
				)
			}
		}
	}

	private fun startMessageRefresh(myInboxPublicKey: String) {
		viewModelScope.launch(Dispatchers.IO) {
			while (isActive) {
				chatRepository.syncMessages(myInboxPublicKey)
				delay(MESSAGE_PULL_TIMEOUT)
			}
		}
	}

	private suspend fun processMessages(originalMessages: List<ChatMessage>): List<ChatMessage> {
		val processedMessages = mutableListOf<ChatMessage>()
		originalMessages.forEach { originalMessage ->

			// replace request with response
			when {
				originalMessage.type == MessageType.APPROVE_REVEAL
					|| originalMessage.type == MessageType.DISAPPROVE_REVEAL
					|| (originalMessage.type == MessageType.REQUEST_MESSAGING && originalMessage.text.isNullOrBlank()) -> Unit // Do nothing
				originalMessage.type == MessageType.REQUEST_REVEAL -> {
					// find nearest response
					val nearestRequestIdentityResponse = getNearestRevealIdentityResponse(originalMessage, originalMessages)
					if (nearestRequestIdentityResponse != null) {
						processedMessages.add(
							// we replace the original message with the response, but with time of the original message
							nearestRequestIdentityResponse.copy(
								time = originalMessage.time
							)
						)
					} else {
						// no response, so we don't replace anything
						processedMessages.add(originalMessage)
					}
				}
				else -> {
					processedMessages.add(originalMessage)
				}
			}

			// find out if the animation is to be done
			if (originalMessage.type == MessageType.APPROVE_REVEAL && !originalMessage.isMine && !originalMessage.isProcessed) {
				_identityRevealed.emit(true)
				viewModelScope.launch(Dispatchers.IO) {
					chatRepository.processMessage(originalMessage)
				}
			}

		}
		return processedMessages.toList()
	}

	private fun getNearestRevealIdentityResponse(
		requestRevealMessage: ChatMessage,
		messages: List<ChatMessage>
	): ChatMessage? {
		return messages.filter {
			// filter reveal identity responses
			it.type == MessageType.APPROVE_REVEAL || it.type == MessageType.DISAPPROVE_REVEAL
		}.filter {
			// filter messages that came after our message
			requestRevealMessage.time < it.time
		}.minByOrNull {
			// and get nearest by time
			it.time
		}
	}

	companion object {
		const val MESSAGE_PULL_TIMEOUT = 20_000L
	}
}