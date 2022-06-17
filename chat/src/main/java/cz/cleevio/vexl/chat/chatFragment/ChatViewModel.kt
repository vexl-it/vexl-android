package cz.cleevio.vexl.chat.chatFragment

import cz.cleevio.repository.model.chat.CommunicationRequest
import cz.cleevio.repository.repository.chat.ChatRepository
import lightbase.core.baseClasses.BaseViewModel

class ChatViewModel constructor(
	private val chatRepository: ChatRepository,
	val communicationRequest: CommunicationRequest
) : BaseViewModel() {

	//todo: get correct keys (should be probably supplied by navArgs)
	val messages = chatRepository.getMessages(
		inboxPublicKey = "xxx", senderPublicKeys = listOf("xxx", "yyy")
	)

}