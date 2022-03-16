package cz.cleevio.network.response.user

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TempKeyPairResponse constructor(
	val privateKey: String,
	val publicKey: String
)
