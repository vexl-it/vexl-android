package cz.cleevio.network.request.chat

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApprovalConfirmRequest constructor(
	val publicKey: String,
	val publicKeyToConfirm: String,
	val signature: String,
	val message: String,
	//true = approve, false = disapprove
	val approve: Boolean
)
