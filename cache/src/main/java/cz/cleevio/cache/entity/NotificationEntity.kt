package cz.cleevio.cache.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity
@JsonClass(generateAdapter = true)
data class NotificationEntity constructor(
	@PrimaryKey val id: Long = NOTIFICATION_ID,
	val token: String,
	val uploaded: Boolean
) {
	companion object {
		private const val NOTIFICATION_ID = 123L
	}
}
