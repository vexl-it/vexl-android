package cz.cleevio.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cz.cleevio.cache.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao : BaseDao<UserEntity> {

	@Query("SELECT * FROM UserEntity")
	fun listAll(): Flow<List<UserEntity>>

	@Query("SELECT * FROM UserEntity LIMIT 1")
	fun getUserFlow(): Flow<UserEntity?>

	@Query("SELECT * FROM UserEntity LIMIT 1")
	suspend fun getUser(): UserEntity?

	@Insert
	override fun insert(item: UserEntity)

	@Query("DELETE FROM UserEntity")
	suspend fun deleteAll()

	@Query(
		"UPDATE UserEntity " +
		"SET anonymousAvatarImageIndex = :anonymousAvatarImageIndex, " +
		"anonymousUsername = :anonymousUsername " +
		"WHERE id == :userId "
	)
	suspend fun updateAnonymousInfo(userId: Long, anonymousUsername: String, anonymousAvatarImageIndex: Int)

	@Query(
		"UPDATE UserEntity " +
			"SET avatar = null " +
			"WHERE id == :userId "
	)
	suspend fun deleteUserAvatar(userId: Long)
}
