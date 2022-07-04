package cz.cleevio.cache.dao

import androidx.room.Dao
import androidx.room.Query
import cz.cleevio.cache.entity.OfferCommonFriendCrossRef

@Dao
interface OfferCommonFriendCrossRefDao : BaseDao<OfferCommonFriendCrossRef> {
	@Query("DELETE FROM OfferCommonFriendCrossRef")
	suspend fun clearTable()
}