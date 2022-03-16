package cz.cleevio.network.response.user

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TempSignatureResponse constructor(
	val signed: String
)
