package cz.cleevio.cache.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["inboxKey", "contactPublicKey"], unique = true)])
data class ChatUserEntity(
	@PrimaryKey(autoGenerate = true)
	val id: Long = 0,
	val contactPublicKey: String,
	val inboxKey: String,
	val name: String,
	val avatar: String?,
	val deAnonymized: Boolean
)