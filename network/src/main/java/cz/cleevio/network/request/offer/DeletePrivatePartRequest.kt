package cz.cleevio.network.request.offer

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeletePrivatePartRequest constructor(
	val adminIds: List<String>,
	val publicKeys: List<String>
)
