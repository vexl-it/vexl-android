package cz.cleevio.network.request.group

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateGroupRequest constructor(
	val name: String,
	val logo: ImageRequest,
	val expirationAt: Long,
	val closureAt: Long,
)
