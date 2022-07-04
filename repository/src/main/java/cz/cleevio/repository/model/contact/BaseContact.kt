package cz.cleevio.repository.model.contact

import android.content.Context
import android.net.Uri
import android.os.Parcelable

abstract class BaseContact(
	open val id: String,
	open val name: String,
	open val photoUri: Uri?,
	open var markedForUpload: Boolean
) : Parcelable {
	abstract fun getIdentifier(): String
	abstract fun getHashedContact(): String
	abstract fun getChatDescription(context: Context): String
}