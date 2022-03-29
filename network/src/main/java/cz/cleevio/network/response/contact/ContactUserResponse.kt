package cz.cleevio.network.response.contact

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContactUserResponse constructor(
	val id: Long,
	val publicKey: String,
	val hash: String
)
