package cz.cleevio.cache.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ContactEntity(
	@PrimaryKey(autoGenerate = true)
	val contactId: Long = 0,
	val contactType: String, // PHONE, FACEBOOK (maybe both?)
	val name: String,
	val phone: String? = null,
	val phoneHashed: String? = null,
	val email: String? = null,
	val photoUri: String? = null,
	val facebookId: String? = null,
	val facebookIdHashed: String? = null
)
