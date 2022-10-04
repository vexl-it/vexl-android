package cz.cleevio.repository.model.chat

import android.os.Parcelable
import com.cleevio.vexl.cryptography.EciesCryptoLib
import com.cleevio.vexl.cryptography.model.KeyPair
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import cz.cleevio.cache.entity.ChatMessageEntity
import cz.cleevio.network.request.chat.ChatMessageRequest
import cz.cleevio.network.request.chat.ChatUserRequest
import cz.cleevio.network.response.chat.MessageResponse
import kotlinx.parcelize.Parcelize
import java.util.*

//do we need to work with public fields from response?
@Parcelize
data class ChatMessage constructor(
	val uuid: String = UUID.randomUUID().toString(),
	val inboxPublicKey: String,
	val senderPublicKey: String,
	val recipientPublicKey: String,
	val text: String? = null,
	val image: String? = null,
	val type: MessageType,
	val time: Long = System.currentTimeMillis(),
	val deanonymizedUser: ChatUser? = null,
	//custom flags
	val isMine: Boolean,
	val isProcessed: Boolean
) : Parcelable

@Parcelize
data class ChatUser constructor(
	val name: String?,
	val image: String?,
	val imageBase64: String?
) : Parcelable

enum class MessageType {
	MESSAGE,
	REQUEST_REVEAL,
	APPROVE_REVEAL,
	DISAPPROVE_REVEAL,
	REQUEST_MESSAGING,
	APPROVE_MESSAGING,
	DISAPPROVE_MESSAGING,
	DELETE_CHAT,
	BLOCK_CHAT,
	BROKEN
}

fun MessageResponse.fromNetwork(inboxKeyPair: KeyPair): ChatMessage {
	//decrypt message string
	val decrypted = EciesCryptoLib.decrypt(inboxKeyPair, this.message)

	//take message field and convert it from String to class
	val moshi = Moshi.Builder().build()
	val jsonAdapter: JsonAdapter<ChatMessageRequest> = moshi.adapter(ChatMessageRequest::class.java)

	val chatMessage = jsonAdapter.fromJson(decrypted)
	return chatMessage?.fromNetwork(
		recipientPublicKey = inboxKeyPair.publicKey,
		inboxPublicKey = inboxKeyPair.publicKey,
		senderPublicKey = senderPublicKey,
		messageType = this.messageType
	) ?: ChatMessage(
		uuid = "broken", type = MessageType.BROKEN, time = System.currentTimeMillis(),
		inboxPublicKey = "", senderPublicKey = "", recipientPublicKey = "",
		isMine = false, isProcessed = false
	)
}

fun ChatMessageRequest.fromNetwork(
	recipientPublicKey: String,
	inboxPublicKey: String,
	senderPublicKey: String,
	messageType: String
): ChatMessage {
	return ChatMessage(
		uuid = this.uuid,
		inboxPublicKey = inboxPublicKey,
		senderPublicKey = senderPublicKey,
		recipientPublicKey = recipientPublicKey,
		text = this.text,
		image = this.image,
		type = MessageType.valueOf(messageType),
		time = this.time,
		deanonymizedUser = this.deanonymizedUser?.fromNetwork(),
		isMine = false,
		isProcessed = false
	)
}

fun ChatMessage.toNetwork(receiverPublicKey: String): String {
	val request = ChatMessageRequest(
		uuid = this.uuid,
		text = this.text,
		image = this.image,
		time = this.time,
		deanonymizedUser = this.deanonymizedUser?.toNetwork()
	)

	val moshi = Moshi.Builder().build()
	val jsonAdapter: JsonAdapter<ChatMessageRequest> = moshi.adapter(ChatMessageRequest::class.java)

	val string = jsonAdapter.toJson(request)
	val result = string?.let {
		EciesCryptoLib.encrypt(receiverPublicKey, it)
	}
	return result ?: ""
}

fun ChatUser.toNetwork(): ChatUserRequest {
	return ChatUserRequest(
		name = this.name,
		image = this.image,
		imageBase64 = this.imageBase64
	)
}

fun ChatUserRequest.fromNetwork(): ChatUser {
	return ChatUser(
		name = this.name,
		image = this.image,
		imageBase64 = this.imageBase64
	)
}

fun ChatMessage.toCache(): ChatMessageEntity = ChatMessageEntity(
	extId = this.uuid,
	inboxPublicKey = this.inboxPublicKey,
	senderPublicKey = this.senderPublicKey,
	recipientPublicKey = this.recipientPublicKey,
	text = this.text,
	image = this.image,
	type = this.type.name,
	time = this.time,
	deAnonName = this.deanonymizedUser?.name,
	deAnonImage = this.deanonymizedUser?.image,
	isMine = this.isMine,
	isProcessed = this.isProcessed
)

// FixMe VEX-1132: Check how to use imageBase64 in the chat message instead of URL image
fun ChatMessageEntity.fromCache(): ChatMessage {
	val chatUser = if (this.deAnonName != null && this.deAnonImage != null) {
		ChatUser(name = this.deAnonName, image = this.deAnonImage, imageBase64 = this.deAnonImage)
	} else {
		null
	}

	return ChatMessage(
		uuid = this.extId,
		inboxPublicKey = this.inboxPublicKey,
		senderPublicKey = this.senderPublicKey,
		recipientPublicKey = this.recipientPublicKey,
		text = this.text,
		image = this.image,
		type = MessageType.valueOf(this.type),
		time = this.time,
		deanonymizedUser = chatUser,
		isMine = this.isMine,
		isProcessed = this.isProcessed
	)
}
