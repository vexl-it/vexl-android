package cz.cleevio.network.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TokenResponse constructor(
	@Json(name = "token_type") val tokenType: String,
	@Json(name = "access_token") val accessToken: String,
	@Json(name = "refresh_token") val refreshToken: String?,
	val type: String
) {

	companion object {
		const val TYPE_GRANT = "GRANT"
		const val TYPE_GENERAL = "GENERAL"
		const val TYPE_REFRESH = "REFRESH"
	}
}