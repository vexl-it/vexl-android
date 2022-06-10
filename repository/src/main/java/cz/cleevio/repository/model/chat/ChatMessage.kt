package cz.cleevio.repository.model.chat

import cz.cleevio.network.response.chat.MessageResponse

//do we need to work with public fields from response?
data class ChatMessage constructor(
	val text: String,
)

fun MessageResponse.fromNetwork(): ChatMessage {
	//todo: take message field and convert it from String to class
	return ChatMessage(text = "dummy text")
}