package cz.cleevio.cache.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserEntity constructor(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val extId: Long,
	val username: String,
	val avatar: String
) {
	override fun toString(): String =
		"User(id='$id', extId='$extId', username='$username', avatar='$avatar')"
}