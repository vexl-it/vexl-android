package cz.cleevio.network.response.group

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GroupCreatedResponse constructor(
	val uuid: String,
	val name: String,
	val expiration: Long,
	val closure: Long,
)
