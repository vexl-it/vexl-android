package cz.cleevio.network.request.offer

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeletePrivatePartRequest constructor(
	val offerId: List<String>,
	val publicKey: List<String>
)
