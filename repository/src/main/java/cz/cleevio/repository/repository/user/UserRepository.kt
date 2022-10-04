package cz.cleevio.repository.repository.user

import cz.cleevio.network.data.Resource
import cz.cleevio.network.request.user.UserRequest
import cz.cleevio.repository.model.UserProfile
import cz.cleevio.repository.model.user.ConfirmCode
import cz.cleevio.repository.model.user.ConfirmPhone
import cz.cleevio.repository.model.user.Signature
import cz.cleevio.repository.model.user.User
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

	suspend fun registerUser(request: UserRequest): Resource<User>

	suspend fun saveUsernameAndAvatar(request: UserRequest): Resource<User>

	//----------------------------------

	suspend fun updateUser(
		username: String? = null, avatar: String? = null, avatarImageExtension: String? = null
	): Resource<User>

	suspend fun deleteAvatar(): Resource<Unit>

	suspend fun deleteLocalUser()

	suspend fun deleteMe(): Resource<Unit>

	suspend fun storeAnonymousUserData(anonymousUsername: String, anonymousAvatarImageIndex: Int)
}
