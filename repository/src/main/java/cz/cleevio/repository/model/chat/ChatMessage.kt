package cz.cleevio.repository.model.chat

import android.os.Parcelable
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
	val image: String?
) : Parcelable

enum class MessageType {
	TEXT, IMAGE, ANON_REQUEST, ANON_REQUEST_RESPONSE, COMMUNICATION_REQUEST,
	COMMUNICATION_REQUEST_RESPONSE,

	// TODO: change to this
	// COMMUNICATION_REQUEST_APPROVED, COMMUNICATION_REQUEST_DENIED,
	DELETE_CHAT, BROKEN
}

fun MessageResponse.fromNetwork(inboxPublicKey: String): ChatMessage {
	//take message field and convert it from String to class
	val moshi = Moshi.Builder().build()
	val jsonAdapter: JsonAdapter<ChatMessageRequest> = moshi.adapter(ChatMessageRequest::class.java)

	val chatMessage = jsonAdapter.fromJson(this.message)
	return chatMessage?.fromNetwork(
		recipientPublicKey = inboxPublicKey,
		inboxPublicKey = inboxPublicKey,
		senderPublicKey = senderPublicKey
	) ?: ChatMessage(
		uuid = "broken", type = MessageType.BROKEN, time = System.currentTimeMillis(),
		inboxPublicKey = "", senderPublicKey = "", recipientPublicKey = "",
		isMine = false, isProcessed = false
	)
}

fun ChatMessageRequest.fromNetwork(
	recipientPublicKey: String,
	inboxPublicKey: String,
	senderPublicKey: String
): ChatMessage {
	return ChatMessage(
		uuid = this.uuid,
		inboxPublicKey = inboxPublicKey,
		senderPublicKey = senderPublicKey,
		recipientPublicKey = recipientPublicKey,
		text = this.text,
		image = this.image,
		type = MessageType.valueOf(this.type),
		time = this.time,
		deanonymizedUser = this.deanonymizedUser?.fromNetwork(),
		isMine = false,
		isProcessed = false
	)
}

fun ChatMessage.toNetwork(): String {
	val request = ChatMessageRequest(
		uuid = this.uuid,
		text = this.text,
		image = this.image,
		type = this.type.name,
		time = this.time,
		deanonymizedUser = this.deanonymizedUser?.toNetwork()
	)

	val moshi = Moshi.Builder().build()
	val jsonAdapter: JsonAdapter<ChatMessageRequest> = moshi.adapter(ChatMessageRequest::class.java)

	val string = jsonAdapter.toJson(request)
	return string ?: ""
}

fun ChatUser.toNetwork(): ChatUserRequest {
	return ChatUserRequest(
		name = this.name,
		image = this.image
	)
}

fun ChatUserRequest.fromNetwork(): ChatUser {
	return ChatUser(
		name = this.name,
		image = this.image
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

fun ChatMessageEntity.fromCache(): ChatMessage {
	val chatUser = if (this.deAnonName != null && this.deAnonImage != null) {
		ChatUser(name = this.deAnonName, image = this.deAnonImage)
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
