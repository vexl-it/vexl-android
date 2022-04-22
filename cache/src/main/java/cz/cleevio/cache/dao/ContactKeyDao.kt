package cz.cleevio.cache.dao

import androidx.room.*
import cz.cleevio.cache.entity.ContactKeyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactKeyDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertContacts(keys: List<ContactKeyEntity>)

	@Query("SELECT * FROM ContactKeyEntity")
	fun getAllKeysFlow(): Flow<List<ContactKeyEntity>>

	@Query("SELECT * FROM ContactKeyEntity")
	fun getAllKeys(): List<ContactKeyEntity>

	@Query("DELETE FROM ContactKeyEntity")
	suspend fun clearTable()

	@Transaction
	suspend fun replaceAll(keys: List<ContactKeyEntity>) {
		clearTable()
		insertContacts(keys)
	}
}