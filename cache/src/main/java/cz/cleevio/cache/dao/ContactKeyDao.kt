package cz.cleevio.cache.dao

import androidx.room.*
import cz.cleevio.cache.entity.ContactKeyEntity
import cz.cleevio.cache.entity.ContactLevel
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactKeyDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertContacts(keys: List<ContactKeyEntity>)

	@Query("SELECT * FROM ContactKeyEntity")
	fun getAllKeysFlow(): Flow<List<ContactKeyEntity>>

	@Query("SELECT * FROM ContactKeyEntity where contactLevel = :contactLevel")
	fun getKeysByLevel(contactLevel: ContactLevel): List<ContactKeyEntity>

	@Query("SELECT * FROM ContactKeyEntity where groupUuid = :groupUuid")
	fun getKeysByGroup(groupUuid: String): List<ContactKeyEntity>

	fun getFirstLevelKeys() =
		getKeysByLevel(ContactLevel.FIRST)

	fun getGroupLevelKeys() =
		getKeysByLevel(ContactLevel.GROUP)

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