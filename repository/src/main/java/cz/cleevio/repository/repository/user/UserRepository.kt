package cz.cleevio.repository.repository.user

import cz.cleevio.cache.entity.User
import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.UserProfile
import cz.cleevio.repository.model.user.ConfirmPhone
import kotlinx.coroutines.flow.Flow

interface UserRepository {

	suspend fun authStepOne(phoneNumber: String): Resource<ConfirmPhone>

	//----------------------------------
	fun getUserFlow(): Flow<User?>

	fun isUserVerified(): Boolean

	suspend fun getUserId(): String?

	suspend fun getUser(): User?

	suspend fun getUserFullname(): UserProfile?

	//----------------------------------
}