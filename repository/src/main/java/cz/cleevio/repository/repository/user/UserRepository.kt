package cz.cleevio.repository.repository.user

import cz.cleevio.cache.entity.User
import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.UserProfile
import cz.cleevio.repository.model.user.ConfirmCode
import cz.cleevio.repository.model.user.ConfirmPhone
import cz.cleevio.repository.model.user.UsernameAvailable
import kotlinx.coroutines.flow.Flow

interface UserRepository {

	suspend fun authStepOne(phoneNumber: String): Resource<ConfirmPhone>

	suspend fun authStepTwo(verificationCode: String, verificationId: Long): Resource<ConfirmCode>

	//----------------------------------
	fun getUserFlow(): Flow<User?>

	fun isUserVerified(): Boolean

	suspend fun getUserId(): String?

	suspend fun getUser(): User?

	suspend fun getUserFullname(): UserProfile?

	//----------------------------------


	suspend fun isUsernameAvailable(username: String): Resource<UsernameAvailable>
}