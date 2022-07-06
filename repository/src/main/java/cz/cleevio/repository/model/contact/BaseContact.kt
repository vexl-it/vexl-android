package cz.cleevio.repository.model.contact

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import cz.cleevio.cache.entity.ContactEntity

abstract class BaseContact(
	open val id: Long,
	open val name: String,
	open val photoUri: Uri?,
	open var markedForUpload: Boolean
) : Parcelable {
	abstract fun getIdentifier(): String
	abstract fun getHashedContact(): String
	abstract fun getChatDescription(context: Context): String
	abstract fun getContactType(): String
}

fun ContactEntity.fromDao(): BaseContact {
	return if (this.contactType == "FACEBOOK") {
		this.fbContactFromDao()
	} else {
		this.phoneContactFromDao()
	}
}

fun ContactEntity.fbContactFromDao(): FacebookContact {
	return FacebookContact(
		id = this.contactId,
		name = this.name,
		facebookId = this.facebookId.orEmpty(),
		photoUri = if (this.photoUri == "null") {
			null
		} else {
			Uri.parse(this.photoUri)
		},
		hashedFacebookId = this.facebookIdHashed.orEmpty()
	)
}

fun ContactEntity.phoneContactFromDao(): Contact {
	return Contact(
		id = this.contactId,
		name = this.name,
		email = this.email.orEmpty(),
		phoneNumber = this.phone.orEmpty(),
		photoUri = if (this.photoUri == "null") {
			null
		} else {
			Uri.parse(this.photoUri)
		},
		hashedPhoneNumber = this.phoneHashed.orEmpty()
	)
}