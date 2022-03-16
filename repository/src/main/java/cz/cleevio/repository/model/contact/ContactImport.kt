package cz.cleevio.repository.model.contact

import cz.cleevio.network.response.contact.ContactImportResponse


data class ContactImport constructor(
	val imported: Boolean,
	val message: String
)

fun ContactImportResponse.fromNetwork(): ContactImport {
	return ContactImport(
		imported = this.imported,
		message = this.message
	)
}
