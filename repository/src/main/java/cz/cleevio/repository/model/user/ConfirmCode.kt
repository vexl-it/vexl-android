package cz.cleevio.repository.model.user

import cz.cleevio.network.response.user.ConfirmCodeResponse

data class ConfirmCode constructor(
	val challenge: String,
	val phoneVerified: Boolean
)

fun ConfirmCodeResponse.fromNetwork() = ConfirmCode(
	challenge = challenge,
	phoneVerified = phoneVerified
)