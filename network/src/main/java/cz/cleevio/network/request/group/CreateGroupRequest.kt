package cz.cleevio.network.request.group

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateGroupRequest constructor(
	val name: String,
	val logo: String,
	val expiration: Long,
	val closureAt: Long,
)
