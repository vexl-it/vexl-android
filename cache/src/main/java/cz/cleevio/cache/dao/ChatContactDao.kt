package cz.cleevio.cache.dao

import androidx.room.Dao
import androidx.room.Query
import cz.cleevio.cache.entity.ChatContactEntity

@Dao
interface ChatContactDao : BaseDao<ChatContactEntity> {

	@Query("DELETE FROM ChatContactEntity")
	fun deleteAll()
}