package cz.cleevio.cache.dao

import androidx.room.Dao
import androidx.room.Query
import cz.cleevio.cache.entity.InboxEntity

@Dao
interface InboxDao : BaseDao<InboxEntity> {

	@Query("SELECT * FROM InboxEntity")
	fun getAllInboxes(): List<InboxEntity>

	@Query("SELECT * FROM InboxEntity WHERE publicKey == :publicKey")
	fun getInboxByPublicKey(publicKey: String): InboxEntity?

	@Query("DELETE FROM InboxEntity")
	fun clearTable()
}