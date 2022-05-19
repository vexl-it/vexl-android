package cz.cleevio.cache.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChatContactEntity(
	@PrimaryKey(autoGenerate = true)
	val id: Long,
	val extId: String,
	//maybe should be two tables?
	val userId: String?,
	//maybe should be two tables?
	val offerId: String?,
	val isApproved: Boolean
)