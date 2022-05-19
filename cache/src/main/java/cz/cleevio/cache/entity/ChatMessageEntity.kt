package cz.cleevio.cache.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChatMessageEntity(
	@PrimaryKey(autoGenerate = true)
	val id: Long,
	val extId: String,
	val text: String,
	val type: String,
	val inboxId: Long
)