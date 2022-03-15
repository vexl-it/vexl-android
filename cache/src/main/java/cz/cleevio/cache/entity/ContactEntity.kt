package cz.cleevio.cache.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class ContactEntity(
	@PrimaryKey(autoGenerate = true)
	val id: Long,
	val name: String,
	val phone: String,
	val email: String,
	val photoUri: String?
)
