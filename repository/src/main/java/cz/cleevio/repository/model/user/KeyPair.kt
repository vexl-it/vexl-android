package cz.cleevio.repository.model.user

import cz.cleevio.network.response.user.TempKeyPairResponse

data class KeyPair constructor(
	val privateKey: String,
	val publicKey: String
)

fun TempKeyPairResponse.fromNetwork(): KeyPair {
	return KeyPair(
		privateKey = this.privateKey,
		publicKey = this.publicKey
	)
}
