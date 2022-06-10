package cz.cleevio.repository.repository.chat

import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.user.User

interface ChatRepository {

	suspend fun createInbox(publicKey: String, firebaseToken: String): Resource<Unit>

	suspend fun sendMessage(senderPublicKey: String, receiverPublicKey: String, message: String, messageType: String): Resource<Unit>

	//would flow be better?
	suspend fun loadMessages(userId: Long?): Resource<List<Any>>

	suspend fun loadChatUsers(): Resource<List<User>>

	suspend fun loadChatRequests(): Resource<List<User>>
}