package cz.cleevio.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cz.cleevio.cache.entity.NotificationEntity

@Dao
interface NotificationDao : BaseDao<NotificationEntity> {

	@Query("SELECT * FROM NotificationEntity LIMIT 1")
	suspend fun getOne(): NotificationEntity?

	@Query("DELETE FROM NotificationEntity")
	suspend fun deleteAll()

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	override suspend fun replace(item: NotificationEntity)
}
