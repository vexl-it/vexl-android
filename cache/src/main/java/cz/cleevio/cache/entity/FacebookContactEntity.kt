package cz.cleevio.cache.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FacebookContactEntity(
	@PrimaryKey(autoGenerate = true)
	val id: Long = 0,
	val name: String,
	val facebookId: String,
	val facebookIdHashed: String,
	val photoUri: String?
)
