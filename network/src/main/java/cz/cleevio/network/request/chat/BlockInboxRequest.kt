package cz.cleevio.network.request.chat

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BlockInboxRequest constructor(
	val publicKey: String,
	val publicKeyToBlock: String,
	val signature: String,
	//true = block, false = unblock
	val block: Boolean
)
