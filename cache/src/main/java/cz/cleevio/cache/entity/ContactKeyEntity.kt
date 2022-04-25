package cz.cleevio.cache.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ContactKeyEntity(
	@PrimaryKey(autoGenerate = true)
	val id: Long = 0,
	val publicKey: String,
	val contectLevel: ContactLevel,
)

enum class ContactLevel {
	FIRST,
	SECOND,
	NOT_SPECIFIED
}