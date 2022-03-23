package cz.cleevio.repository.model.user

import cz.cleevio.network.response.user.SignatureResponse

data class Signature constructor(
	val hash: String,
	val signature: String,
	val challengeVerified: Boolean
)

fun SignatureResponse.fromNetwork(): Signature {
	return Signature(
		hash = this.hash,
		signature = this.signature,
		challengeVerified = this.challengeVerified,
	)
}
