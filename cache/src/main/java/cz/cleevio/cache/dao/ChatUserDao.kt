package cz.cleevio.cache.dao

import androidx.room.Dao
import androidx.room.Query
import cz.cleevio.cache.entity.ChatUserIdentityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatUserDao : BaseDao<ChatUserIdentityEntity> {

	@Query(
		"UPDATE ChatUserIdentityEntity " +
			"SET name=:name, avatarBase64=:avatarBase64, deAnonymized=1 " +
			"WHERE contactPublicKey=:contactPublicKey " +
			" AND inboxKey=:inboxKey"
	)
	fun deAnonymizeUser(name: String, avatarBase64: String?, contactPublicKey: String, inboxKey: String)

	@Query(
		"SELECT * " +
			"FROM ChatUserIdentityEntity " +
			"WHERE contactPublicKey=:contactPublicKey " +
			" AND inboxKey=:inboxKey"
	)
	fun getUserIdentity(inboxKey: String, contactPublicKey: String): ChatUserIdentityEntity?

	@Query(
		"SELECT * " +
			"FROM ChatUserIdentityEntity " +
			"WHERE contactPublicKey=:contactPublicKey"
	)
	fun getUserByContactKey(contactPublicKey: String): ChatUserIdentityEntity?

	@Query(
		"SELECT * " +
			"FROM ChatUserIdentityEntity " +
			"WHERE contactPublicKey=:contactPublicKey " +
			" AND inboxKey=:inboxKey"
	)
	fun getUserIdentityFlow(inboxKey: String, contactPublicKey: String): Flow<ChatUserIdentityEntity?>

	@Query("DELETE FROM ChatUserIdentityEntity")
	suspend fun clearTable()
}