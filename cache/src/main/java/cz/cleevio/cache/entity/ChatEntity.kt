package cz.cleevio.cache.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["publicKey"], unique = true)])
data class ChatEntity(
	//----- metadata -----
	@PrimaryKey(autoGenerate = true)
	val id: Long = 0,
	//InboxType enum
	val inboxType: String,
	val publicKey: String,
	val privateKey: String,
	//if type is InboxType.OFFER
	val offerId: String?
)