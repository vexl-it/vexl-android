package cz.cleevio.repository.model.contact

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import cz.cleevio.cache.entity.ContactEntity
import kotlinx.parcelize.Parcelize
import java.util.*

@Suppress("DataClassShouldBeImmutable")
@Parcelize
data class Contact constructor(
	override val id: Long,
	override val name: String,
	val email: String,
	val phoneNumber: String,
	override val photoUri: Uri?,
	override var markedForUpload: Boolean = true,
	//will be computed just in time before upload to BE
	val hashedPhoneNumber: String = ""
) : BaseContact(
	id = id,
	name = name,
	photoUri = photoUri,
	markedForUpload = markedForUpload
), Parcelable {

	fun phoneOrEmail(): String {
		return if (phoneNumber.isNotEmpty()) {
			phoneNumber.replace("\\s".toRegex(), "")
		} else {
			email
		}
	}

	fun getInitials(): String {
		val parts = name.split(" ")
		var result = ""

		parts.forEach {
			result += it.firstOrNull() ?: ""
		}

		return result.uppercase(Locale.getDefault())
	}

	override fun getIdentifier(): String = phoneNumber

	override fun getHashedContact(): String = hashedPhoneNumber

	override fun getChatDescription(context: Context): String =
		phoneNumber

	override fun getContactType(): String =
		"PHONE"
}

fun Contact.toDao(): ContactEntity = ContactEntity(
	contactId = this.id,
	contactType = this.getContactType(),
	name = this.name,
	phone = this.phoneNumber,
	phoneHashed = this.hashedPhoneNumber,
	email = this.email,
	photoUri = this.photoUri.toString()
)