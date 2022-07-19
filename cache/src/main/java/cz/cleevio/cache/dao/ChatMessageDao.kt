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
	fun getLatestBySenders(inboxPublicKey: String, firstKey: String, secondKey: String): ChatMessageEntity?

	//get all unique public keys of persons you have talked with
	@Query(
		"SELECT DISTINCT senderPublicKey " +
			"FROM ChatMessageEntity " +
			"WHERE senderPublicKey != inboxPublicKey " +
			" AND type NOT IN ('COMMUNICATION_REQUEST')"
		//TODO: change to this
		// " AND type NOT IN ('COMMUNICATION_REQUEST', 'COMMUNICATION_REQUEST_DENIED')"
	)
	fun getAllContactKeys(): List<String>

	@Suppress("FunctionMaxLength")
	@Query(
		"SELECT * " +
			"FROM ChatMessageEntity " +
			"WHERE type == 'COMMUNICATION_REQUEST' " +
			"AND isMine == 0 " +
			"AND isProcessed == 0 " +
			"ORDER BY time"
	)
	fun listAllPendingCommunicationMessages(): List<ChatMessageEntity>

	@Query(
		"SELECT * " +
			"FROM ChatMessageEntity " +
			"WHERE type == 'ANON_REQUEST' " +
			"AND inboxPublicKey == :inboxPublicKey " +
			" AND (senderPublicKey == :firstKey AND recipientPublicKey == :secondKey) " +
			" OR (senderPublicKey == :secondKey AND recipientPublicKey == :firstKey) " +
			"AND isMine == 0 " +
			"AND isProcessed == 0 " +
			"ORDER BY time"
	)
	fun listPendingIdentityRevealsBySenders(inboxPublicKey: String, firstKey: String, secondKey: String): Flow<List<ChatMessageEntity>>

	@Query(
		"UPDATE ChatMessageEntity " +
			"SET isProcessed = 1 " +
			"WHERE type == 'ANON_REQUEST' " +
			"AND inboxPublicKey == :inboxPublicKey " +
			" AND (senderPublicKey == :firstKey AND recipientPublicKey == :secondKey) " +
			" OR (senderPublicKey == :secondKey AND recipientPublicKey == :firstKey) " +
			"AND isMine == 0 " +
			"AND isProcessed == 0 "
	)
	fun solvePendingIdentityRevealsBySenders(inboxPublicKey: String, firstKey: String, secondKey: String)

	@Query("DELETE FROM ChatMessageEntity")
	fun deleteAll()

	@Query(
		"DELETE " +
			"FROM ChatMessageEntity " +
			"WHERE inboxPublicKey == :inboxPublicKey" +
			" AND (senderPublicKey == :firstKey AND recipientPublicKey == :secondKey)" +
			" OR (senderPublicKey == :secondKey AND recipientPublicKey == :firstKey) "
	)
	fun deleteByKeys(inboxPublicKey: String, firstKey: String, secondKey: String)
}