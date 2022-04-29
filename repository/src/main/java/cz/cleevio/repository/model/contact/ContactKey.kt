package cz.cleevio.repository.model.contact

import cz.cleevio.cache.entity.ContactKeyEntity

data class ContactKey constructor(
	val key: String,
	val level: ContactLevel
)

fun ContactKeyEntity.fromCache(): ContactKey {
	return ContactKey(
		key = this.publicKey,
		level = this.contactLevel.fromCache()
	)
}