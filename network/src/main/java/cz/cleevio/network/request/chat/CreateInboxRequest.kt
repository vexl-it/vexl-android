package cz.cleevio.network.request.chat

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateInboxRequest constructor(
	//public key of user or offer
	val publicKey: String,
	//firebase token
	val token: String
)
