package cz.cleevio.cache.dao

import androidx.room.Dao
import androidx.room.Query
import cz.cleevio.cache.entity.ChatUserEntity

@Dao
interface ChatUserDao : BaseDao<ChatUserEntity> {

	@Query(
		"UPDATE ChatUserEntity " +
			"SET name=:name, avatar=:avatar, deAnonymized=1 " +
			"WHERE contactPublicKey=:contactPublicKey " +
			" AND inboxKey=:inboxKey"
	)
	fun deAnonymizeUser(name: String, avatar: String?, contactPublicKey: String, inboxKey: String)

}