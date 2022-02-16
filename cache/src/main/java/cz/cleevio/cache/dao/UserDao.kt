package cz.cleevio.cache.dao

import androidx.room.Dao
import androidx.room.Query
import cz.cleevio.cache.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao : BaseDao<User> {

	@Query("SELECT * FROM User")
	fun listAll(): Flow<List<User>>

	@Query("SELECT * FROM User LIMIT 1")
	fun getUserFlow(): Flow<User?>

	@Query("SELECT * FROM User LIMIT 1")
	suspend fun getUser(): User?

	@Query("DELETE FROM User")
	fun deleteAll()
}
