package cz.cleevio.repository.repository.user

import cz.cleevio.cache.dao.UserDao
import cz.cleevio.cache.entity.User
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.repository.model.UserProfile
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl constructor(
	private val userDao: UserDao,
	private val encryptedPreference: EncryptedPreferenceRepository
) : UserRepository {

	override fun getUserFlow(): Flow<User?> = userDao.getUserFlow()

	override fun isUserVerified(): Boolean = encryptedPreference.isUserVerified

	override suspend fun getUserId(): String? = userDao.getUser()?.id

	override suspend fun getUser(): User? = userDao.getUser()

	override suspend fun getUserFullname(): UserProfile? {
		val user = userDao.getUser() ?: return null
		return UserProfile(
			fullname = user.getFullname(),
			photoUrl = user.photoUrl
		)
	}
}