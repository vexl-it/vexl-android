package cz.cleevio.repository.repository.user

import cz.cleevio.cache.dao.UserDao
import cz.cleevio.cache.entity.UserEntity
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.network.api.UserApi
import cz.cleevio.network.data.Resource
import cz.cleevio.network.extensions.tryOnline
import cz.cleevio.network.request.user.ConfirmChallengeRequest
import cz.cleevio.network.request.user.ConfirmCodeRequest
import cz.cleevio.network.request.user.ConfirmPhoneRequest
import cz.cleevio.repository.model.UserProfile
import cz.cleevio.repository.model.user.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

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

	override suspend fun authStepThree(signature: String): Resource<Signature> {
		return tryOnline(
			mapper = {
				it?.fromNetwork()
			},
			request = {
				userRestApi.postUserConfirmChallenge(
					ConfirmChallengeRequest(
						userPublicKey = encryptedPreference.userPublicKey,
						signature = signature
					)
				)
			}
		)
	}

	override suspend fun registerFacebookUser(facebookId: String): Resource<Signature> = tryOnline(
		request = { userRestApi.getUserSignatureFacebook(facebookId = facebookId) },
		mapper = { it?.fromNetwork() },
		doOnSuccess = {
			it?.let { data ->
				encryptedPreference.facebookHash = data.hash
				encryptedPreference.facebookSignature = data.signature
			}
		}
	)

	override fun getUserFlow(): Flow<User?> = userDao.getUserFlow().map { it?.fromDao() }

	override fun isUserVerified(): Boolean = encryptedPreference.isUserVerified

	override suspend fun createUser(user: User) {
		userDao.deleteAll()
		userDao.insert(
			UserEntity(
				username = user.username,
				avatar = null,
				avatarBase64 = user.avatarBase64,
				publicKey = user.publicKey
			)
		)
	}

	override suspend fun markUserFinishedOnboarding(user: User) {
		userDao.update(
			UserEntity(
				id = user.id ?: 1,
				username = user.username,
				anonymousUsername = user.anonymousUsername,
				avatar = null,
				avatarBase64 = user.avatarBase64,
				anonymousAvatarImageIndex = user.anonymousAvatarImageIndex,
				publicKey = user.publicKey,
				finishedOnboarding = true
			)
		)
	}

	override suspend fun getUserId(): Long? = userDao.getUser()?.id

	override suspend fun getUser(): User? = userDao.getUser()?.fromDao()

	override suspend fun getUserFullname(): UserProfile? {
		val user = userDao.getUser() ?: return null
		return UserProfile(
			fullname = user.username,
			avatar = user.avatar,
			avatarBase64 = user.avatarBase64
		)
	}

	override suspend fun updateUser(
		username: String?, avatar: String?
	) {
		val currentUserData = userDao.getUser()?.fromDao() ?: return
		if (username != null) {
			updateUser(user = currentUserData.copy(username = username))
		} else {
			updateUser(user = currentUserData.copy(avatarBase64 = avatar))
		}
	}

	override suspend fun deleteAvatar() {
		userDao.getUser()?.id?.let {
			userDao.deleteUserAvatar(it)
		}
	}

	override suspend fun deleteLocalUser() =
		userDao.deleteAll()

	override suspend fun storeAnonymousUserData(anonymousUsername: String, anonymousAvatarImageIndex: Int) {
		userDao.getUser()?.id?.let {
			Timber.d("user: ${userDao.getUser()}")
			userDao.updateAnonymousInfo(it, anonymousUsername, anonymousAvatarImageIndex)
		} ?: Timber.d("user: No user found")

	}

	private suspend fun updateUser(user: User) {
		userDao.update(
			UserEntity(
				id = getUserId() ?: 0,
				username = user.username,
				anonymousUsername = userDao.getUser()?.anonymousUsername,
				avatar = null,
				avatarBase64 = user.avatarBase64 ?: userDao.getUser()?.avatarBase64, // If there's new local avatar replace it
				anonymousAvatarImageIndex = userDao.getUser()?.anonymousAvatarImageIndex,
				publicKey = user.publicKey,
				finishedOnboarding = user.finishedOnboarding
			)
		)
	}
}
