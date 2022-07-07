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

	@Query(
		"SELECT * " +
			"FROM ContactEntity " +
			"WHERE (contactType = 'PHONE' AND phoneHashed IN (:hashes)) " +
			"OR (contactType = 'FACEBOOK' AND facebookIdHashed IN (:hashes))"
	)
	fun getContactByHashes(hashes: Collection<String>): List<ContactEntity>

	@Query("DELETE FROM ContactEntity where contactType = :contactType")
	suspend fun clearTableByType(contactType: String)
}