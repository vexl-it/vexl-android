package cz.cleevio.cache.dao

import androidx.room.Query
import cz.cleevio.cache.entity.ChatEntity

interface ChatDao : BaseDao<ChatEntity> {

	@Query("SELECT * FROM ChatEntity")
	fun getAllChats(): List<ChatEntity>

	@Query("SELECT * FROM ChatEntity")
	fun getChatByPublicKey(): ChatEntity
}