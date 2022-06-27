package cz.cleevio.cache.dao

import androidx.room.*
import cz.cleevio.cache.entity.OfferEntity
import cz.cleevio.cache.entity.OfferWithLocations
import kotlinx.coroutines.flow.Flow

@Dao
interface OfferDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertOffer(offer: OfferEntity): Long

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertOffers(offers: List<OfferEntity>)

	@Query("SELECT * FROM OfferEntity")
	fun getAllOffers(): List<OfferEntity>

	@Query("SELECT * FROM OfferEntity")
	fun getAllOffersWithLocations(): List<OfferWithLocations>

	@Query("SELECT * FROM OfferEntity")
	fun getAllOffersWithLocationsFlow(): Flow<List<OfferWithLocations>>

	@Query("DELETE FROM OfferEntity")
	suspend fun clearTable()

	@Query("DELETE FROM OfferEntity WHERE offerId in (:offerIds)")
	suspend fun deleteOffersById(offerIds: List<String>)

	@Query("SELECT * FROM OfferEntity WHERE offerId == :offerId")
	fun getOfferById(offerId: String): OfferWithLocations

	@Transaction
	suspend fun replaceAll(offers: List<OfferEntity>) {
		clearTable()
		insertOffers(offers)
	}
}