package cz.cleevio.repository.repository.user

import cz.cleevio.cache.dao.UserDao
import cz.cleevio.cache.entity.UserEntity
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.network.api.UserApi
import cz.cleevio.network.data.Resource
import cz.cleevio.network.extensions.tryOnline
import cz.cleevio.network.request.user.ConfirmCodeRequest
import cz.cleevio.network.request.user.ConfirmPhoneRequest
import cz.cleevio.network.request.user.UserRequest
import cz.cleevio.network.request.user.UsernameAvailableRequest
import cz.cleevio.repository.model.UserProfile
import cz.cleevio.repository.model.user.*
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl constructor(
	private val userRestApi: UserApi,
	private val userDao: UserDao,
	private val encryptedPreference: EncryptedPreferenceRepository
) : UserRepository {

	override suspend fun authStepOne(phoneNumber: String): Resource<ConfirmPhone> {
		return tryOnline(
			mapper = {
				it?.fromNetwork()
			},
			request = { userRestApi.postUserConfirmPhone(ConfirmPhoneRequest(phoneNumber = phoneNumber)) }
		)
	}

	override suspend fun authStepTwo(verificationCode: String, verificationId: Long): Resource<ConfirmCode> {
		return tryOnline(
			mapper = {
				it?.fromNetwork()
			},
			request = {
				userRestApi.postUserConfirmCode(
					ConfirmCodeRequest(
						id = verificationId,
						code = verificationCode,
						//fixme: either connect it here via encryption helper class or add new param
						userPublicKey = ""
					)
				)
			}
		)
	}

	override fun getUserFlow(): Flow<UserEntity?> = userDao.getUserFlow()

	override fun isUserVerified(): Boolean = encryptedPreference.isUserVerified

	override suspend fun createUser(extId: Long, username: String, avatar: String) {
		userDao.insert(
			UserEntity(
				extId = extId,
				username = username,
				avatar = avatar
			)
		)
	}

	override suspend fun getUserId(): Long? = userDao.getUser()?.id

	override suspend fun getUser(): UserEntity? = userDao.getUser()

	override suspend fun getUserFullname(): UserProfile? {
		val user = userDao.getUser() ?: return null
		return UserProfile(
			fullname = user.username,
			photoUrl = user.avatar
		)
	}

	override suspend fun registerUser(username: String, avatar: String): Resource<UserRegistration> {
		return tryOnline(
			doOnSuccess = {
				it?.let {
					createUser(
						it.userId,
						it.username,
						it.avatar
					)
				}
			},
			mapper = {
				it?.fromNetwork()
			},
			request = {
				userRestApi.postUser(
					UserRequest(
						username = username,
						avatar = avatar
					)
				)
			}
		)
	}

	override suspend fun isUsernameAvailable(username: String): Resource<UsernameAvailable> {
		return tryOnline(
					mapper = {
						it?.fromNetwork(username)
					},
			request = { userRestApi.postUserUsernameAvailable(UsernameAvailableRequest(username = username)) }
		)
	}
}
