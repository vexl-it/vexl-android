package cz.cleevio.repository.model.user

import cz.cleevio.network.response.user.UserResponse

data class UserRegistration constructor(
	val userId: Long,
	val username: String,
	val avatar: String,
	val publicKey: String
)

fun UserResponse.fromNetwork() = UserRegistration(
	userId = userId,
	username = username,
	avatar = avatar,
	publicKey = publicKey
)