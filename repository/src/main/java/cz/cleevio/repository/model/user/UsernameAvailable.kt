package cz.cleevio.repository.model.user

import cz.cleevio.network.response.user.UsernameAvailableResponse

data class UsernameAvailable constructor(
	val available: Boolean,
	val username: String
)

fun UsernameAvailableResponse.fromNetwork(username: String) = UsernameAvailable(
	available = isAvailable,
	username = username
)