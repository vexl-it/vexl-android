package cz.cleevio.network.request.chat

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApprovalRequest constructor(
	//public key of inbox you want approval from
	val publicKey: String,
	val message: String
)
