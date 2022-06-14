package cz.cleevio.cache.dao

import androidx.room.Dao
import androidx.room.Query
import cz.cleevio.cache.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao : BaseDao<ChatMessageEntity> {

	@Query(
		"SELECT * FROM ChatMessageEntity WHERE inboxPublicKey LIKE :inboxPublicKey" +
			" AND senderPublicKey IN (:senderPublicKeys)  ORDER BY time"
	)
	fun listAllBySenders(inboxPublicKey: String, senderPublicKeys: List<String>): Flow<List<ChatMessageEntity>>

	@Query("DELETE FROM ChatMessageEntity")
	fun deleteAll()
}