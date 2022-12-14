package cz.cleevio.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cz.cleevio.cache.entity.LocationEntity

@Dao
interface LocationDao : BaseDao<LocationEntity> {

	@Query("SELECT * FROM LocationEntity")
	suspend fun getAll(): List<LocationEntity>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertLocations(locations: List<LocationEntity>)

	@Query("DELETE FROM LocationEntity")
	suspend fun clearTable()
}