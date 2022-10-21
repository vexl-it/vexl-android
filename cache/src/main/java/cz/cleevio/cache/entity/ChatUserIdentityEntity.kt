package cz.cleevio.cache.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["inboxKey", "contactPublicKey"], unique = true)])
data class ChatUserIdentityEntity(
	@PrimaryKey(autoGenerate = true)
	val id: Long = 0,
	val contactPublicKey: String,
	val inboxKey: String,
	val name: String? = null,
	val anonymousUsername: String? = null,
	@Deprecated("Avatar url is not used anymore, use avatarBase64")
	val avatar: String? = null,
	val avatarBase64: String? = null,
	val anonymousAvatarImageIndex: Int? = null,
	val deAnonymized: Boolean
)