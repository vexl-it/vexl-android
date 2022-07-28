package cz.cleevio.repository.model.group

import cz.cleevio.cache.entity.GroupEntity
import cz.cleevio.network.response.group.GroupCreatedResponse
import cz.cleevio.network.response.group.GroupResponse

data class Group constructor(
	val groupUuid: String,
	val name: String,
	val logoUrl: String? = null,
	val createdAt: Long = 0,
	val expirationAt: Long,
	val closureAt: Long,
	val code: Long = 0,
	val memberCount: Long = 0,
)

fun GroupCreatedResponse.fromNetwork(): Group = Group(
	groupUuid = this.uuid,
	name = this.name,
	expirationAt = this.expiration,
	closureAt = this.closure
)

fun GroupResponse.fromNetwork(): Group = Group(
	groupUuid = this.uuid,
	name = this.name,
	logoUrl = this.logoUrl,
	createdAt = this.createdAt,
	expirationAt = this.expirationAt,
	closureAt = this.closureAt,
	code = this.code,
	memberCount = this.memberCount
)

fun Group.toEntity(): GroupEntity = GroupEntity(
	groupUuid = this.groupUuid,
	name = this.name,
	logoUrl = this.logoUrl,
	createdAt = this.createdAt,
	expirationAt = this.expirationAt,
	closureAt = this.closureAt,
	code = this.code
)

fun GroupEntity.fromEntity(): Group = Group(
	groupUuid = this.groupUuid,
	name = this.name,
	logoUrl = this.logoUrl,
	createdAt = this.createdAt,
	expirationAt = this.expirationAt,
	closureAt = this.closureAt,
	code = this.code
)
