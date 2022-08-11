package cz.cleevio.cache.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserEntity constructor(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	//todo: remove later
	val extId: Long = 0,
	val username: String,
	val anonymousUsername: String? = null,
	val avatar: String?,
	val anonymousAvatarImageIndex: Int? = null,
	val publicKey: String,
	val finishedOnboarding: Boolean = false
) {
	override fun toString(): String =
		"User(id='$id', username='$username', anonymousUsername='$anonymousUsername', " +
			"avatar='$avatar', anonymousAvatarImageIndex='$anonymousAvatarImageIndex', finishedOnboarding='$finishedOnboarding')"
}