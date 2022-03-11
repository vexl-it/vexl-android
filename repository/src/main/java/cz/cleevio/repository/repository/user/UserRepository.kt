package cz.cleevio.repository.repository.user

import cz.cleevio.cache.entity.UserEntity
import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.UserProfile
import cz.cleevio.repository.model.user.ConfirmCode
import cz.cleevio.repository.model.user.ConfirmPhone
import cz.cleevio.repository.model.user.UserRegistration
import cz.cleevio.repository.model.user.UsernameAvailable
import kotlinx.coroutines.flow.Flow

interface UserRepository {

	suspend fun authStepOne(phoneNumber: String): Resource<ConfirmPhone>

	suspend fun authStepTwo(verificationCode: String, verificationId: Long): Resource<ConfirmCode>

	//----------------------------------
	fun getUserFlow(): Flow<UserEntity?>

	fun isUserVerified(): Boolean

	suspend fun createUser(extId: Long, username: String, avatar: String)

	suspend fun getUserId(): Long?

	suspend fun getUser(): UserEntity?

	suspend fun getUserFullname(): UserProfile?

	suspend fun registerUser(username: String, avatar: String): Resource<UserRegistration>

	//----------------------------------

	suspend fun isUsernameAvailable(username: String): Resource<UsernameAvailable>
}