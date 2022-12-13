package cz.cleevio.network.request.chat

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateChallengeBatchRequest constructor(
	val publicKeys: List<String>
)
