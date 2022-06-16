package cz.cleevio.repository.model.chat

import cz.cleevio.network.response.chat.ChallengeCreatedResponse

data class ChatChallenge constructor(
	val challenge: String,
	val expiration: Long,
	val signature: String? = null
)

fun ChallengeCreatedResponse.fromNetwork(): ChatChallenge = ChatChallenge(
	challenge = this.challenge,
	expiration = this.expiration.toLong()
)
