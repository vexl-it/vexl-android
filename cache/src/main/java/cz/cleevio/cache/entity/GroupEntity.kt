package cz.cleevio.cache.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["groupUuid"], unique = true)])
data class GroupEntity constructor(
	@PrimaryKey(autoGenerate = true)
	val id: Long = 0,
	val groupUuid: String,
	val name: String,
	val logoUrl: String?,
	val createdAt: Long,
	val expirationAt: Long,
	val closureAt: Long,
	val code: Long,
	val memberCount: Long
)
