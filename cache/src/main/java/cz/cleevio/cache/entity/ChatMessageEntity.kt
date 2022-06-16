package cz.cleevio.cache.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChatMessageEntity(
	//----- metadata -----
	@PrimaryKey(autoGenerate = true)
	val id: Long = 0,
	val extId: String,
	val inboxPublicKey: String,
	val senderPublicKey: String,

	//------ data --------
	val text: String?,
	val image: String?,
	val type: String,
	val time: Long,
	val deAnonName: String?,
	val deAnonImage: String?,
)