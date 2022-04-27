package cz.cleevio.cache.dao

import androidx.room.*
import cz.cleevio.cache.entity.OfferEntity

@Dao
interface OfferDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertOffers(offers: List<OfferEntity>)

	@Query("SELECT * FROM OfferEntity")
	fun getAllOffers(): List<OfferEntity>

	@Query("DELETE FROM OfferEntity")
	suspend fun clearTable()

	@Transaction
	suspend fun replaceAll(keys: List<OfferEntity>) {
		clearTable()
		insertOffers(keys)
	}
}