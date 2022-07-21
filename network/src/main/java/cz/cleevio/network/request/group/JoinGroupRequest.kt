package cz.cleevio.network.request.group

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JoinGroupRequest constructor(
	val code: Long
)
