package cz.cleevio.network.response.contact

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContactResponse constructor(
	val publicKey: String
)
