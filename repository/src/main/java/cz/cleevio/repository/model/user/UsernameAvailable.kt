package cz.cleevio.repository.model.user

import cz.cleevio.network.response.user.UsernameAvailableResponse

data class UsernameAvailable constructor(
	val available: Boolean
)

fun UsernameAvailableResponse.fromNetwork() = UsernameAvailable(
	available = available,
)