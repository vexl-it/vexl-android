package cz.cleevio.network.request.chat

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SendMessageBatchRequest constructor(
	val senderPublicKey: String,
	val messages: List<BatchMessage>,
	val signedChallenge: SignedChallengeRequest
)