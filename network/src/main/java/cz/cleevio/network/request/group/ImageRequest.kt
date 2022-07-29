package cz.cleevio.network.request.group

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImageRequest constructor(
	val extension: String,
	val data: String
)
