package cz.cleevio.repository.repository.chat

import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.network.api.ChatApi
import cz.cleevio.network.data.Resource
import cz.cleevio.network.extensions.tryOnline
import cz.cleevio.repository.model.user.User
import timber.log.Timber

class ChatRepositoryImpl constructor(
	private val chatApi: ChatApi,
	private val encryptedPreferenceRepository: EncryptedPreferenceRepository
) : ChatRepository {

	override suspend fun sendMessage(): Resource<Unit> = tryOnline(
		request = { chatApi.postMessage("fake_message") },
		mapper = { }
	)

	override suspend fun loadMessages(userId: Long?): Resource<List<Any>> {
		Timber.d("${encryptedPreferenceRepository.isUserVerified}") //does nothing, fixes detekt
		//todo: return messages from DB
		return Resource.success(
			data = listOf<String>(
				"Message one",
				"Message two"
			)
		)
	}

	override suspend fun loadChatUsers(): Resource<List<User>> {
		return Resource.success(
			listOf(
				User(
					id = 1,
					extId = 10,
					username = "Friend 1",
					avatar = "url",
					publicKey = "xxYY"
				),
				User(
					id = 2,
					extId = 11,
					username = "Friend 2",
					avatar = "url",
					publicKey = "xxYY"
				)
			)
		)
	}

	override suspend fun loadChatRequests(): Resource<List<User>> {
		return Resource.success(
			listOf(
				User(
					id = 155,
					extId = 100,
					username = "Unknown Friend 1",
					avatar = "url",
					publicKey = "xxYYzz"
				),
				User(
					id = 156,
					extId = 101,
					username = "Unknown Friend 2",
					avatar = "url",
					publicKey = "xxYYzz"
				)
			)
		)
	}
}