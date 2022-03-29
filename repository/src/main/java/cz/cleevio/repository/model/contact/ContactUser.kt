package cz.cleevio.repository.model.contact

import cz.cleevio.network.response.contact.ContactUserResponse

data class ContactUser constructor(
	val id: Long,
	val publicKey: String,
	val hash: String
)

fun ContactUserResponse.fromNetwork(): ContactUser {
	return ContactUser(
		id = this.id,
		publicKey = this.publicKey,
		hash = this.hash
	)
}
