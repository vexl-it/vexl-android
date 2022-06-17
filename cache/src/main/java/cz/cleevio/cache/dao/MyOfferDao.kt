package cz.cleevio.cache.dao

import androidx.room.Dao
import androidx.room.Query
import cz.cleevio.cache.entity.MyOfferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MyOfferDao : BaseDao<MyOfferEntity> {

	@Query("SELECT * FROM MyOfferEntity")
	fun listAllFlow(): Flow<List<MyOfferEntity>>

	@Query("SELECT * FROM MyOfferEntity where extId = :offerId LIMIT 1")
	suspend fun getMyOfferById(offerId: String): MyOfferEntity?

	@Query("SELECT * FROM MyOfferEntity where publicKey = :publicKey LIMIT 1")
	suspend fun getMyOfferByPublicKey(publicKey: String): MyOfferEntity?

	@Query("SELECT publicKey FROM MyOfferEntity")
	suspend fun getAllOfferKeys(): List<String>

	@Query("SELECT COUNT(id) FROM MyOfferEntity where offerType  = :offerType")
	suspend fun getMyOfferCount(offerType: String): Int
}