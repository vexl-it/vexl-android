package cz.cleevio.network.request.chat

data class CreateInboxRequest constructor(
	//public key of user or offer
	val publicKey: String,
	//firebase token
	val token: String
)
