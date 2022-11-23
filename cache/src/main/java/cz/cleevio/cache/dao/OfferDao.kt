package cz.cleevio.cache.dao

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import cz.cleevio.cache.entity.OfferEntity
import cz.cleevio.cache.entity.OfferWithLocationsAndCommonFriends
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
	fun getAllExtendedOffers(): List<OfferWithLocationsAndCommonFriends>

	@Query("SELECT * FROM OfferEntity")
	fun getAllExtendedOffersFlow(): Flow<List<OfferWithLocationsAndCommonFriends>>

	@RawQuery(observedEntities = [OfferWithLocationsAndCommonFriends::class])
	fun getFilteredOffersFlow(query: SupportSQLiteQuery): Flow<List<OfferWithLocationsAndCommonFriends>>

	@Query("DELETE FROM OfferEntity")
	suspend fun clearTable()

	@Query("DELETE FROM OfferEntity WHERE externalOfferId in (:offerIds)")
	suspend fun deleteOffersById(offerIds: List<String>)

	@Query("SELECT * FROM OfferEntity WHERE externalOfferId == :offerId")
	fun getOfferById(offerId: String): OfferWithLocationsAndCommonFriends?

	@Query("SELECT COUNT(offerId) FROM OfferEntity WHERE offerType == :offerType AND isMine == 1 AND active == 1")
	fun getMyActiveOffersCount(offerType: String): Int

	@Query("SELECT COUNT(offerId) FROM OfferEntity WHERE offerType == :offerType AND isMine == 1")
	fun getMyOffersCount(offerType: String): Int

	@Transaction
	suspend fun replaceAll(offers: List<OfferEntity>) {
		clearTable()
		insertOffers(offers)
	}
}