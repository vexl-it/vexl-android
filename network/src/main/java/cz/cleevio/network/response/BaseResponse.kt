package cz.cleevio.network.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BaseResponse constructor(
	val code: String? = null,
	val message: List<String>? = null
)