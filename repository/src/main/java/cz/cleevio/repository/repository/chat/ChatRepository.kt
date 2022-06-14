package cz.cleevio.repository.repository.chat

import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.chat.ChatMessage
import cz.cleevio.repository.model.user.User
import kotlinx.coroutines.flow.SharedFlow

interface ChatRepository {

	suspend fun saveFirebasePushToken(token: String)

	//-----------------------------
	suspend fun createInbox(publicKey: String): Resource<Unit>

	suspend fun syncMessages(keyPair: KeyPair): Resource<Unit>

	suspend fun deleteMessagesFromBE(publicKey: String): Resource<Unit>

	fun getMessages(inboxPublicKey: String, senderPublicKeys: List<String>): SharedFlow<List<ChatMessage>>

	suspend fun sendMessage(senderPublicKey: String, receiverPublicKey: String, message: String, messageType: String): Resource<Unit>

	//would flow be better?
	suspend fun loadMessages(userId: Long?): Resource<List<Any>>

	suspend fun loadChatUsers(): Resource<List<User>>

	suspend fun loadChatRequests(): Resource<List<User>>
}