package cz.cleevio.cache.dao

import androidx.room.Dao
import androidx.room.Query
import cz.cleevio.cache.entity.ChatMessageEntity

@Dao
interface ChatMessageDao : BaseDao<ChatMessageEntity> {

	@Query("DELETE FROM ChatMessageEntity")
	fun deleteAll()
}