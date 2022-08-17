package cz.cleevio.network.request.offer

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeletePrivatePartRequest constructor(
	val offerIds: List<String>,
	val publicKeys: List<String>
)
