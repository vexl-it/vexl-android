package cz.cleevio.network.request.chat

data class ApprovalRequest constructor(
	//public key of inbox you want approval from
	val publicKey: String,
	val message: String
)
