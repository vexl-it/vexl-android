package cz.cleevio.repository.model.contact

import cz.cleevio.cache.entity.ContactKeyEntity

data class ContactKey constructor(
	val key: String,
	val level: ContactLevel,
	val groupUuid: String?,
	val isUpToDate: Boolean,
)

fun ContactKeyEntity.fromCache(): ContactKey {
	return ContactKey(
		key = this.publicKey,
		level = this.contactLevel.fromCache(),
		groupUuid = this.groupUuid,
		isUpToDate = this.isUpToDate,
	)
}

fun ContactKey.toCache(): ContactKeyEntity {
	return ContactKeyEntity(
		publicKey = this.key,
		contactLevel = this.level.toCache(),
		groupUuid = this.groupUuid,
		isUpToDate = this.isUpToDate
	)
}
