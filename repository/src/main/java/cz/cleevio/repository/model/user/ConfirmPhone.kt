package cz.cleevio.repository.model.user

import cz.cleevio.network.response.user.ConfirmPhoneResponse

data class ConfirmPhone constructor(
	val verificationId: Long,
	val expirationAt: String
)

fun ConfirmPhoneResponse.fromNetwork() = ConfirmPhone(
	verificationId = verificationId,
	expirationAt = expirationAt
)
