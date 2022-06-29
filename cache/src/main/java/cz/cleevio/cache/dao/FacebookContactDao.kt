package cz.cleevio.cache.dao

import androidx.room.*
import cz.cleevio.cache.entity.FacebookContactEntity

@Dao
interface FacebookContactDao : BaseDao<FacebookContactEntity> {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertContacts(contactEntity: List<FacebookContactEntity>)

	@Query("DELETE FROM FacebookContactEntity")
	suspend fun clearTable()

	@Transaction
	suspend fun replaceAllContacts(contactEntity: List<FacebookContactEntity>) {
		clearTable()
		insertContacts(contactEntity)
	}
}