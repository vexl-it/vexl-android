package cz.cleevio.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cz.cleevio.cache.entity.ContactKeyEntity
import cz.cleevio.cache.entity.ContactLevel
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactKeyDao : BaseDao<ContactKeyEntity> {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertContacts(keys: List<ContactKeyEntity>)

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertGroupContacts(keys: List<ContactKeyEntity>)

	@Query("SELECT * FROM ContactKeyEntity")
	fun getAllKeysFlow(): Flow<List<ContactKeyEntity>>

	@Query("SELECT * FROM ContactKeyEntity WHERE contactLevel == :contactLevel")
	fun getKeysByLevel(contactLevel: ContactLevel): List<ContactKeyEntity>

	@Query("DELETE FROM ContactKeyEntity WHERE contactLevel == :contactLevel")
	fun deleteKeysByLevel(contactLevel: ContactLevel)

	@Query("DELETE FROM ContactKeyEntity WHERE groupUuid == :groupUuid")
	fun deleteKeysByGroupUuid(groupUuid: String)

	@Query("DELETE FROM ContactKeyEntity WHERE groupUuid == :groupUuid AND publicKey == :publicKey")
	fun deleteKeysByGroupUuidAndPublicKey(groupUuid: String, publicKey: String)

	@Query("SELECT * FROM ContactKeyEntity WHERE groupUuid == :groupUuid")
	fun getKeysByGroup(groupUuid: String): List<ContactKeyEntity>

	fun getFirstLevelKeys() =
		getKeysByLevel(ContactLevel.FIRST)

	fun getSecondLevelKeys() =
		getKeysByLevel(ContactLevel.SECOND)

	fun getGroupLevelKeys() =
		getKeysByLevel(ContactLevel.GROUP)

	fun deleteFirstLevelKeys() =
		deleteKeysByLevel(ContactLevel.FIRST)

	fun deleteSecondLevelKeys() =
		deleteKeysByLevel(ContactLevel.SECOND)

	fun deleteGroupLevelKeys() =
		deleteKeysByLevel(ContactLevel.GROUP)

	@Query("SELECT * FROM ContactKeyEntity")
	fun getAllKeys(): List<ContactKeyEntity>

	@Query("DELETE FROM ContactKeyEntity")
	suspend fun clearTable()

	@Query("SELECT * FROM ContactKeyEntity WHERE (publicKey == :publicKey AND groupUuid != :groupUuid)")
	fun findKeyOutsideThisGroup(publicKey: String, groupUuid: String): ContactKeyEntity?

}