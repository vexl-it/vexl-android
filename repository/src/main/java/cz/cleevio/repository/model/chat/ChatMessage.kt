package cz.cleevio.repository.model.chat

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import cz.cleevio.cache.entity.ChatMessageEntity
import cz.cleevio.network.response.chat.MessageResponse


//do we need to work with public fields from response?
@JsonClass(generateAdapter = true)
data class ChatMessage constructor(
	val uuid: String,
	val inboxPublicKey: String,
	val senderPublicKey: String,
	val text: String? = null,
	val image: String? = null,
	val type: MessageType,
	val time: Long,
	val deanonymizedUser: ChatUser? = null
)

@JsonClass(generateAdapter = true)
data class ChatUser constructor(
	val name: String?,
	val image: String?
)

enum class MessageType {
	TEXT, IMAGE, ANON_REQUEST, ANON_REQUEST_RESPONSE, COMMUNICATION_REQUEST, COMMUNICATION_REQUEST_RESPONSE, DELETE_CHAT, BROKEN
}

fun MessageResponse.fromNetwork(inboxPublicKey: String): ChatMessage {
	//take message field and convert it from String to class
	val moshi = Moshi.Builder().build()
	val jsonAdapter: JsonAdapter<ChatMessage> = moshi.adapter(ChatMessage::class.java)

	val chatMessage = jsonAdapter.fromJson(this.message)
		?.copy(inboxPublicKey = inboxPublicKey, senderPublicKey = this.senderPublicKey)
	return chatMessage ?: ChatMessage(
		uuid = "broken", type = MessageType.BROKEN, time = System.currentTimeMillis(),
		inboxPublicKey = "", senderPublicKey = ""
	)
}

fun ChatMessage.toNetwork(): String {
	val moshi = Moshi.Builder().build()
	val jsonAdapter: JsonAdapter<ChatMessage> = moshi.adapter(ChatMessage::class.java)

	val string = jsonAdapter.toJson(this)
	return string ?: ""
}

fun ChatMessage.toCache(): ChatMessageEntity = ChatMessageEntity(
	extId = this.uuid,
	inboxPublicKey = this.inboxPublicKey,
	senderPublicKey = this.senderPublicKey,
	text = this.text,
	image = this.image,
	type = this.type.name,
	time = this.time,
	deAnonName = this.deanonymizedUser?.name,
	deAnonImage = this.deanonymizedUser?.image
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
		text = this.text,
		image = this.image,
		type = MessageType.valueOf(this.type),
		time = this.time,
		deanonymizedUser = chatUser
	)
}
