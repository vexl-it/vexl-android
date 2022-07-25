package cz.cleevio.repository.model.chat

import cz.cleevio.cache.entity.ChatUserIdentityEntity

data class ChatUserIdentity(
	val id: Long,
	val contactPublicKey: String,
	val inboxKey: String,
	val name: String,
	val avatar: String?,
	val deAnonymized: Boolean
)

fun ChatUserIdentityEntity.fromCache(): ChatUserIdentity {
	return ChatUserIdentity(
		id = this.id,
		contactPublicKey = this.contactPublicKey,
		inboxKey = this.inboxKey,
		name = this.name,
		avatar = this.avatar,
		deAnonymized = this.deAnonymized
	)
}