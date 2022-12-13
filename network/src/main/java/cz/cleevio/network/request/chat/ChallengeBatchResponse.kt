package cz.cleevio.network.request.chat

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChallengeBatchResponse constructor(
	val challenges: List<ChallengeResponse>,
	val expiration: String
)
