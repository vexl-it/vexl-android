package cz.cleevio.network.response.chat

data class ChallengeCreatedResponse constructor(
	val challenge: String,
	val expiration: String
)
