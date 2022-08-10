package cz.cleevio.cache.dao

import androidx.room.Dao
import androidx.room.Query
import cz.cleevio.cache.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao : BaseDao<GroupEntity> {

	@Query("SELECT * FROM GroupEntity")
	fun getAllGroups(): List<GroupEntity>

	@Query("SELECT * FROM GroupEntity")
	fun getAllGroupsFlow(): Flow<List<GroupEntity>>

	@Query("DELETE FROM GroupEntity WHERE groupUuid == :groupUuid")
	suspend fun deleteByUuid(groupUuid: String)

	@Query("DELETE FROM GroupEntity")
	suspend fun clearTable()
}