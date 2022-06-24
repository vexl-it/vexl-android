package cz.cleevio.cache.dao

import androidx.room.Dao
import androidx.room.Query
import cz.cleevio.cache.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao : BaseDao<ChatMessageEntity> {

	@Query(
		"SELECT * " +
			"FROM ChatMessageEntity " +
			"WHERE inboxPublicKey == :inboxPublicKey" +
			" AND (senderPublicKey == :firstKey AND recipientPublicKey == :secondKey)" +
			" OR (senderPublicKey == :secondKey AND recipientPublicKey == :firstKey) " +
			"ORDER BY time"
	)
	fun listAllBySenders(inboxPublicKey: String, firstKey: String, secondKey: String): Flow<List<ChatMessageEntity>>

	@Query(
		"SELECT * " +
			"FROM ChatMessageEntity " +
			"WHERE inboxPublicKey == :inboxPublicKey" +
			" AND (senderPublicKey == :firstKey AND recipientPublicKey == :secondKey)" +
			" OR (senderPublicKey == :secondKey AND recipientPublicKey == :firstKey) " +
			"ORDER BY time DESC " +
			"LIMIT 1"
	)
	fun getLatestBySenderAndRecipientKeys(inboxPublicKey: String, firstKey: String, secondKey: String): ChatMessageEntity?

	//get all unique public keys of persons you have talked with
	@Query(
		"SELECT DISTINCT senderPublicKey FROM ChatMessageEntity WHERE senderPublicKey != inboxPublicKey AND type NOT IN ('COMMUNICATION_REQUEST', 'COMMUNICATION_REQUEST_RESPONSE')"
	)
	fun getAllContactKeys(): List<String>

	@Query(
		"SELECT * " +
			"FROM ChatMessageEntity " +
			"WHERE type == 'COMMUNICATION_REQUEST' " +
			"AND isMine == 0 " +
			"AND isProcessed == 0 " +
			"ORDER BY time"
	)
	fun listAllPendingCommunicationMessages(): List<ChatMessageEntity>

	@Query("DELETE FROM ChatMessageEntity")
	fun deleteAll()
}