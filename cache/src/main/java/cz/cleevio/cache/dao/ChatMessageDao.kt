package cz.cleevio.cache.dao

import androidx.room.Dao
import androidx.room.Query
import cz.cleevio.cache.entity.ChatMessageEntity
import cz.cleevio.cache.entity.MessageKeyPair
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao : BaseDao<ChatMessageEntity> {

	@Query(
		"SELECT * " +
			"FROM ChatMessageEntity " +
			"WHERE inboxPublicKey == :inboxPublicKey" +
			" AND ((senderPublicKey == :firstKey AND recipientPublicKey == :secondKey)" +
			" OR (senderPublicKey == :secondKey AND recipientPublicKey == :firstKey)) " +
			"ORDER BY time"
	)
	fun listAllBySenders(inboxPublicKey: String, firstKey: String, secondKey: String): Flow<List<ChatMessageEntity>>

	@Query(
		"SELECT * " +
			"FROM ChatMessageEntity " +
			"WHERE inboxPublicKey == :inboxPublicKey" +
			" AND ((senderPublicKey == :firstKey AND recipientPublicKey == :secondKey)" +
			" OR (senderPublicKey == :secondKey AND recipientPublicKey == :firstKey)) " +
			"ORDER BY time DESC " +
			"LIMIT 1"
	)
	fun getLatestBySenders(inboxPublicKey: String, firstKey: String, secondKey: String): ChatMessageEntity?

	//get all unique public keys of persons you have talked with
	//we want all different sender x recipient unique combinations
	@Query(
		"SELECT senderPublicKey, recipientPublicKey " +
			"FROM ChatMessageEntity " +
			"WHERE " +
			"inboxPublicKey = :inboxPublicKey " +
			" AND NOT (type == 'REQUEST_MESSAGING' AND isMine=0)" +
			" AND NOT (type == 'DISAPPROVE_MESSAGING' AND isMine=1)" +
			"GROUP BY senderPublicKey, recipientPublicKey"
	)
	fun getAllContactKeys(inboxPublicKey: String): List<MessageKeyPair>

	@Suppress("FunctionMaxLength")
	@Query(
		"SELECT * " +
			"FROM ChatMessageEntity " +
			"WHERE type == 'REQUEST_MESSAGING' " +
			"AND isMine == 0 " +
			"AND isProcessed == 0 " +
			"ORDER BY time"
	)
	fun listAllPendingCommunicationMessages(): List<ChatMessageEntity>

	@Suppress("FunctionMaxLength")
	@Query(
		"SELECT * " +
			"FROM ChatMessageEntity " +
			"WHERE type == 'REQUEST_REVEAL' " +
			"AND inboxPublicKey == :inboxPublicKey " +
			" AND ((senderPublicKey == :firstKey AND recipientPublicKey == :secondKey) " +
			" OR (senderPublicKey == :secondKey AND recipientPublicKey == :firstKey)) " +
			"AND isMine == 0 " +
			"AND isProcessed == 0 " +
			"ORDER BY time"
	)
	fun listPendingIdentityRevealsBySenders(
		inboxPublicKey: String,
		firstKey: String,
		secondKey: String
	): Flow<List<ChatMessageEntity>>

	@Query(
		"SELECT * " +
			"FROM ChatMessageEntity " +
			"WHERE  inboxPublicKey == :inboxPublicKey " +
			" AND (" +
			"	(senderPublicKey == :firstKey AND recipientPublicKey == :secondKey) " +
			" 	OR (senderPublicKey == :secondKey AND recipientPublicKey == :firstKey)" +
			" ) " +
			" AND (" +
			"	(type == 'REQUEST_REVEAL' AND isProcessed = 0) " +
			"   OR type == 'APPROVE_REVEAL'" +
			" )" +
			"ORDER BY time DESC"
	)
	fun listPendingAndApprovedIdentityReveals(
		inboxPublicKey: String,
		firstKey: String,
		secondKey: String
	): Flow<List<ChatMessageEntity>>

	@Query(
		"SELECT * " +
			"FROM ChatMessageEntity " +
			"WHERE type == 'REQUEST_REVEAL' " +
			"AND inboxPublicKey == :inboxPublicKey " +
			" AND (senderPublicKey == :userPublicKey AND recipientPublicKey == :myPublicKey) " +
			"AND isProcessed == 0 AND isMine = 0 " +
			"ORDER BY time DESC " +
			"LIMIT 1"
	)
	fun getLastRequestRevealMessageByUser(
		inboxPublicKey: String, userPublicKey: String,
		myPublicKey: String
	): ChatMessageEntity?

	@Suppress("FunctionMaxLength")
	@Query(
		"UPDATE ChatMessageEntity " +
			"SET isProcessed = 1 " +
			"WHERE type == 'REQUEST_REVEAL' " +
			"AND inboxPublicKey == :inboxPublicKey " +
			" AND ((senderPublicKey == :firstKey AND recipientPublicKey == :secondKey) " +
			" OR (senderPublicKey == :secondKey AND recipientPublicKey == :firstKey)) " +
			"AND isProcessed == 0 "
	)
	fun solvePendingIdentityRevealsBySenders(inboxPublicKey: String, firstKey: String, secondKey: String)

	@Query("DELETE FROM ChatMessageEntity")
	fun deleteAll()

	@Query(
		"DELETE " +
			"FROM ChatMessageEntity " +
			"WHERE inboxPublicKey == :inboxPublicKey" +
			" AND ((senderPublicKey == :firstKey AND recipientPublicKey == :secondKey)" +
			" OR (senderPublicKey == :secondKey AND recipientPublicKey == :firstKey)) "
	)
	fun deleteByKeys(inboxPublicKey: String, firstKey: String, secondKey: String)

	@Query("SELECT * FROM ChatMessageEntity")
	fun listAllFlow(): Flow<List<ChatMessageEntity>>
}