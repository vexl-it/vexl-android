package cz.cleevio.network.request.chat

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeletionBatchRequest constructor(
	val dataForRemoval: List<DeletionRequest>
)
