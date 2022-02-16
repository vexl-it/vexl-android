package cz.cleevio.cache.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

@Suppress("unused")
interface BaseDao<T> {

	@Insert
	fun insert(item: T)

	@Insert
	suspend fun insertAll(items: List<T>)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun replace(item: T)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun replaceAll(items: List<T>)

	@Update
	suspend fun update(item: T)

	@Update
	suspend fun updateAll(items: List<T>)

	@Delete
	suspend fun delete(item: T)

	@Delete
	suspend fun deleteAll(items: List<T>)
}