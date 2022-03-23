package cz.cleevio.repository.model.user

import cz.cleevio.network.request.user.TempSignatureRequest

data class TempSignature constructor(
	val challenge: String,
	val privateKey: String
)

fun TempSignature.toRequest(): TempSignatureRequest {
	return TempSignatureRequest(
		challenge = this.challenge,
		privateKey = this.privateKey
	)
}