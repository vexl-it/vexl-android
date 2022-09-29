package cz.cleevio.network.request.offer

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeletePrivatePartRequest constructor(
	//field name has to be `adminIds`
	val adminIds: List<String>,
	val publicKeys: List<String>
)
