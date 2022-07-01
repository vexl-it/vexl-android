package cz.cleevio.cache.dao

import androidx.room.Dao
import androidx.room.Query
import cz.cleevio.cache.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao : BaseDao<ContactEntity> {

	@Query("SELECT photoUri FROM ContactEntity where name = :name")
	suspend fun getContactPhotoUriByName(name: String): String?

	@Query("SELECT * FROM ContactEntity")
	fun getAllContactsFlow(): Flow<List<ContactEntity>>

	@Query("SELECT * FROM ContactEntity")
	fun getAllContacts(): List<ContactEntity>

	@Query("SELECT * FROM ContactEntity WHERE phoneHashed IN (:hashedPhones)")
	fun getContactByHashedPhones(hashedPhones: Collection<String>): List<ContactEntity>

	@Query("DELETE FROM ContactEntity")
	suspend fun clearTable()
}