package cz.cleevio.repository.model.user

import cz.cleevio.cache.entity.UserEntity
import cz.cleevio.network.response.user.UserResponse

data class User constructor(
	val id: Long?,
	val extId: Long,
	val username: String,
	val avatar: String,
	val publicKey: String
)

fun UserResponse.fromNetwork() = User(
	id = null,
	extId = userId,
	username = username,
	avatar = avatar,
	publicKey = publicKey
)

fun UserEntity.fromDao() = User(
	id = id,
	extId = extId,
	username = username,
	avatar = avatar,
	publicKey = publicKey
)