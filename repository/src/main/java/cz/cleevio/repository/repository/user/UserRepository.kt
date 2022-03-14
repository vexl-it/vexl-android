package cz.cleevio.repository.repository.user

import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.UserProfile
import cz.cleevio.repository.model.user.ConfirmCode
import cz.cleevio.repository.model.user.ConfirmPhone
import cz.cleevio.repository.model.user.User
import cz.cleevio.repository.model.user.UsernameAvailable
import kotlinx.coroutines.flow.Flow

interface UserRepository {

	suspend fun authStepOne(phoneNumber: String): Resource<ConfirmPhone>

	suspend fun authStepTwo(verificationCode: String, verificationId: Long): Resource<ConfirmCode>

	//----------------------------------
	fun getUserFlow(): Flow<User?>

	fun isUserVerified(): Boolean

	suspend fun createUser(user: User)

	suspend fun getUserId(): Long?

	suspend fun getUser(): User?

	suspend fun getUserFullname(): UserProfile?

	suspend fun registerUser(username: String, avatar: String): Resource<User>

	//----------------------------------

	suspend fun isUsernameAvailable(username: String): Resource<UsernameAvailable>
}