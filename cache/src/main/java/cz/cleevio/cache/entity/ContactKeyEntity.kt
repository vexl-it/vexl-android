package cz.cleevio.cache.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["publicKey", "groupUuid"], unique = true)])
data class ContactKeyEntity(
	@PrimaryKey(autoGenerate = true)
	val id: Long = 0,
	val publicKey: String,
	val contactLevel: ContactLevel,
	val groupUuid: String? = null,
	//should not be used anymore, should stay here until we have way to change data models
	val isUpToDate: Boolean
)

enum class ContactLevel {
	FIRST,
	SECOND,
	GROUP,
	NOT_SPECIFIED
}