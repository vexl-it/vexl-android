package cz.cleevio.cache.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["extId"], unique = true)])
data class ChatMessageEntity(
	//----- metadata -----
	@PrimaryKey(autoGenerate = true)
	val id: Long = 0,
	val extId: String,
	val inboxPublicKey: String,
	val senderPublicKey: String,
	val recipientPublicKey: String,

	//------ data --------
	val text: String?,
	val image: String?,
	val type: String,
	val time: Long,
	val deAnonName: String?,
	@Deprecated("Avatar url is not used anymore, use avatarBase64")
	val deAnonImage: String? = null,
	val deAnonImageBase64: String?,
	@ColumnInfo(defaultValue = "-1")
	val sortingIdFromBE: Long,
	//custom flags
	val isMine: Boolean,
	val isProcessed: Boolean
)