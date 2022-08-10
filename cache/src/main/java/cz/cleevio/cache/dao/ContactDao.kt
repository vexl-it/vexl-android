package cz.cleevio.cache.dao

import androidx.room.Dao
import androidx.room.Query
import cz.cleevio.cache.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao : BaseDao<ContactEntity> {

	@Query("SELECT * FROM ContactEntity")
	fun getAllContactsFlow(): Flow<List<ContactEntity>>

	@Query("SELECT * FROM ContactEntity where contactType = 'PHONE'")
	fun getAllPhoneContacts(): List<ContactEntity>

	@Query("SELECT * FROM ContactEntity where contactType = 'FACEBOOK'")
	fun getAllFacebookContacts(): List<ContactEntity>

	@Query("SELECT * FROM ContactEntity")
	fun getAllContacts(): List<ContactEntity>

	@Query("DELETE FROM ContactEntity where contactType = :contactType")
	suspend fun clearTableByType(contactType: String)

	@Query("DELETE FROM ContactEntity")
	suspend fun clearTable()
}