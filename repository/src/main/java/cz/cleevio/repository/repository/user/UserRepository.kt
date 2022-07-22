package cz.cleevio.repository.repository.user

import cz.cleevio.network.data.Resource
import cz.cleevio.network.request.user.UserAvatar
import cz.cleevio.repository.model.UserProfile
import cz.cleevio.repository.model.user.*
import kotlinx.coroutines.flow.Flow

interface UserRepository {

	suspend fun authStepOne(phoneNumber: String): Resource<ConfirmPhone>

	suspend fun authStepTwo(verificationCode: String, verificationId: Long): Resource<ConfirmCode>

	suspend fun authStepThree(signature: String): Resource<Signature>

	suspend fun registerFacebookUser(facebookId: String): Resource<Signature>

	//----------------------------------
	fun getUserFlow(): Flow<User?>

	fun isUserVerified(): Boolean

	suspend fun createUser(user: User)

	suspend fun markUserFinishedOnboarding(user: User)

	suspend fun getUserId(): Long?

	suspend fun getUser(): User?

	suspend fun getUserMe(): Resource<User?>

	suspend fun getUserFullname(): UserProfile?

	suspend fun registerUser(username: String, avatar: UserAvatar?): Resource<User>

	//----------------------------------

	suspend fun isUsernameAvailable(username: String): Resource<UsernameAvailable>

	suspend fun deleteMe(): Resource<Unit>
}