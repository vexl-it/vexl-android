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
import kotlinx.coroutines.flow.map

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
						userPublicKey = encryptedPreference.userPublicKey
					)
				)
			}
		)
	}

	override fun getUserFlow(): Flow<User?> = userDao.getUserFlow().map { it?.fromDao() }

	override fun isUserVerified(): Boolean = encryptedPreference.isUserVerified

	override suspend fun createUser(user: User) {
		userDao.insert(
			UserEntity(
				extId = user.extId,
				username = user.username,
				avatar = user.avatar,
				publicKey = user.publicKey
			)
		)
	}

	override suspend fun getUserId(): Long? = userDao.getUser()?.id

	override suspend fun getUser(): User? = userDao.getUser()?.fromDao()

	override suspend fun getUserFullname(): UserProfile? {
		val user = userDao.getUser() ?: return null
		return UserProfile(
			fullname = user.username,
			photoUrl = user.avatar
		)
	}

	override suspend fun registerUser(username: String, avatar: String): Resource<User> {
		return tryOnline(
			doOnSuccess = {
				it?.let {
					createUser(it)
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

	override suspend fun getFakeKeyPairFromBE(): Resource<KeyPair> = tryOnline(
		request = { userRestApi.getTempKeyPairs() },
		mapper = { it?.fromNetwork() }
	)

	override suspend fun getFakeSignatureFromBE(signature: Signature): Resource<String> = tryOnline(
		request = { userRestApi.getTempSignature(signature.toRequest()) },
		mapper = { it?.signed }
	)
}
