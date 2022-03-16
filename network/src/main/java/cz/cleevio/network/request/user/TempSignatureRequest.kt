package cz.cleevio.network.request.user

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TempSignatureRequest constructor(
	val challenge: String,
	val privateKey: String
)
