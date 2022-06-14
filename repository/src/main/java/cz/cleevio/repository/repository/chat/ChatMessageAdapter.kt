package cz.cleevio.repository.repository.chat

import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import cz.cleevio.repository.model.chat.ChatMessage

class ChatMessageAdapter {

	@FromJson
	fun fromJson(data: String): ChatMessage {
		val moshi = Moshi
			.Builder()
			.build()
		return moshi.adapter(ChatMessage::class.java).fromJson(data)!!
	}

	@ToJson
	fun toJson(value: ChatMessage): String {
		val moshi = Moshi
			.Builder()
			.build()
		return moshi.adapter(ChatMessage::class.java).toJson(value)
	}
}