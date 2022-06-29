package cz.cleevio.cache.dao

import androidx.room.Dao
import androidx.room.Query
import cz.cleevio.cache.entity.RequestedOfferEntity

@Dao
interface RequestedOfferDao : BaseDao<RequestedOfferEntity> {

	@Query("SELECT * FROM RequestedOfferEntity")
	fun listAll(): List<RequestedOfferEntity>

}