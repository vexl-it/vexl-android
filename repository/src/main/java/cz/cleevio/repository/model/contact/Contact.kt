package cz.cleevio.repository.model.contact

import android.net.Uri
import android.os.Parcelable
import cz.cleevio.cache.entity.ContactEntity
import kotlinx.parcelize.Parcelize
import java.util.*


@Parcelize
data class Contact constructor(
	var id: String,
	var name: String,
	var email: String,
	var phoneNumber: String,
	var photoUri: Uri?,
) : Parcelable {

	constructor() : this(
		id = "",
		name = "",
		email = "",
		phoneNumber = "",
		photoUri = null,
	)

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
}

fun ContactEntity.fromDao(): Contact {
	return Contact(
		id = this.id.toString(),
		name = this.name,
		email = this.email,
		phoneNumber = this.phone,
		photoUri = if (this.photoUri == "null") {
			null
		} else {
			Uri.parse(this.photoUri)
		}
	)
}
