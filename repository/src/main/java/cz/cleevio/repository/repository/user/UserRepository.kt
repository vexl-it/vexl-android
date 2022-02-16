package cz.cleevio.repository.repository.user

import cz.cleevio.cache.entity.User
import cz.cleevio.repository.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {

	fun getUserFlow(): Flow<User?>

	fun isUserVerified(): Boolean

	suspend fun getUserId(): String?

	suspend fun getUser(): User?

	suspend fun getUserFullname(): UserProfile?
}