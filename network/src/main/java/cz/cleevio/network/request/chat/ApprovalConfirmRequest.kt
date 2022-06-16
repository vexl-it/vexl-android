package cz.cleevio.network.request.chat

data class ApprovalConfirmRequest constructor(
	val publicKey: String,
	val publicKeyToConfirm: String,
	val signature: String,
	val message: String,
	//true = approve, false = disapprove
	val approve: Boolean
)
