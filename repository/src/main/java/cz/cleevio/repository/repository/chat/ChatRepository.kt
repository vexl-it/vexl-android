package cz.cleevio.repository.repository.chat

import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.cache.entity.MessageKeyPair
import cz.cleevio.network.data.Resource
import cz.cleevio.network.response.chat.MessageResponse
import cz.cleevio.network.response.chat.MessagesResponse
import cz.cleevio.repository.model.chat.ChatListUser
import cz.cleevio.repository.model.chat.ChatMessage
import cz.cleevio.repository.model.chat.ChatUserIdentity
import cz.cleevio.repository.model.chat.CommunicationRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

interface ChatRepository {

	val chatUsersFlow: MutableSharedFlow<List<ChatListUser>>
	var chatUsers: List<ChatListUser>

	suspend fun saveFirebasePushToken(token: String)

	//-----------------------------
	suspend fun createInbox(publicKey: String, offerId: String? = null): Resource<Unit>

	suspend fun syncMessages(inboxPublicKey: String): Resource<Unit>

	suspend fun syncMessages(keyPair: KeyPair): Resource<Unit>

	suspend fun syncAllMessages(): Resource<Unit>

	suspend fun deleteMessagesFromBE(publicKey: String): Resource<Unit>

	fun getMessages(inboxPublicKey: String, firstKey: String, secondKey: String): SharedFlow<List<ChatMessage>>

	suspend fun sendMessage(
		senderPublicKey: String,
		receiverPublicKey: String,
		message: ChatMessage,
		messageType: String,
	): Resource<MessageResponse>

	suspend fun sendMessageBatch(
		messages: Map<String, List<ChatMessage>>, // sender public key, V
		inboxKeys: List<String>
	): Resource<MessagesResponse>

	suspend fun processMessage(message: ChatMessage)

	suspend fun changeUserBlock(senderKeyPair: KeyPair, publicKeyToBlock: String, block: Boolean): Resource<Unit>

	suspend fun askForCommunicationApproval(
		publicKey: String,
		offerId: String,
		message: ChatMessage
	): Resource<MessageResponse>

	suspend fun confirmCommunicationRequest(
		offerId: String,
		publicKeyToConfirm: String,
		message: ChatMessage,
		originalRequestMessage: ChatMessage,
		approve: Boolean
	): Resource<MessageResponse>

	suspend fun deleteInbox(publicKey: String): Resource<Unit>

	suspend fun deleteAllInboxes(publicKeys: List<String>): Resource<Unit>

	suspend fun loadCommunicationRequests(): List<CommunicationRequest>

	suspend fun startEmittingChatUsers()

	suspend fun getOneChatUser(messageKeyPair: MessageKeyPair): ChatListUser?

	suspend fun deleteMessage(communicationRequest: CommunicationRequest)

	suspend fun getMyInboxKeys(): List<String>

	suspend fun getKeyPairByMyPublicKey(myPublicKey: String): KeyPair?

	suspend fun clearChatTables()

	fun getPendingIdentityRequest(
		inboxPublicKey: String,
		firstKey: String,
		secondKey: String
	): Flow<Boolean>

	fun hasPendingDeleteChatRequest(
		inboxPublicKey: String,
		firstKey: String,
		secondKey: String
	): Flow<Boolean>

	fun getPendingDeleteChatRequest(
		inboxPublicKey: String,
		firstKey: String,
		secondKey: String
	): List<ChatMessage>

	fun canRequestIdentity(
		inboxPublicKey: String,
		firstKey: String,
		secondKey: String
	): Flow<Boolean>

	fun solveIdentityRevealRequest(
		inboxPublicKey: String,
		firstKey: String,
		secondKey: String
	)

	fun getChatUserIdentityFlow(inboxKey: String, contactPublicKey: String): Flow<ChatUserIdentity?>

	suspend fun deAnonymizeUser(contactPublicKey: String, inboxKey: String, myPublicKey: String)

	//should be called only once, when migrating from `1.0.9` up
	suspend fun createInboxesForMeAndOffers()

	suspend fun deleteChat(deleteMessage: ChatMessage)
}