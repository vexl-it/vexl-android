package cz.cleevio.cache.dao

import androidx.room.Dao
import androidx.room.Query
import cz.cleevio.cache.entity.ReportedOfferEntity

@Dao
interface ReportedOfferDao : BaseDao<ReportedOfferEntity> {

	@Query("SELECT * FROM ReportedOfferEntity")
	fun listAll(): List<ReportedOfferEntity>

	@Query("SELECT offerId FROM ReportedOfferEntity")
	fun listAllIds(): List<String>

	@Query("DELETE FROM ReportedOfferEntity")
	suspend fun clearTable()
}