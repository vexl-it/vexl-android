package cz.cleevio.repository.model.contact

import android.net.Uri

abstract class BaseContact(
	open val id: String,
	open val name: String,
	open val photoUri: Uri?,
	open var markedForUpload: Boolean
) {
	abstract fun getIdentifier(): String
	abstract fun getHashedContact(): String
}