package cz.cleevio.cache.dao

import androidx.room.Dao
import androidx.room.Query
import cz.cleevio.cache.entity.ChatUserIdentityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatUserDao : BaseDao<ChatUserIdentityEntity> {

	@Query(
		"UPDATE ChatUserIdentityEntity " +
			"SET name=:name, avatar=:avatar, deAnonymized=1 " +
			"WHERE contactPublicKey=:contactPublicKey " +
			" AND inboxKey=:inboxKey"
	)
	fun deAnonymizeUser(name: String, avatar: String?, contactPublicKey: String, inboxKey: String)

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
			"WHERE contactPublicKey=:contactPublicKey " +
			" AND inboxKey=:inboxKey"
	)
	fun getUserIdentityFlow(inboxKey: String, contactPublicKey: String): Flow<ChatUserIdentityEntity?>
}