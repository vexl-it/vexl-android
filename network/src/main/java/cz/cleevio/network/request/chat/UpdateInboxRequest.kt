package cz.cleevio.network.request.chat

data class UpdateInboxRequest constructor(
	//public key of user or offer
	val publicKey: String,
	//firebase token
	val token: String
)