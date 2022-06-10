package cz.cleevio.network.request.chat

data class BlockInboxRequest constructor(
	val publicKey: String,
	val publicKeyToBlock: String,
	val signature: String,
	//true = block, false = unblock
	val block: Boolean
)
