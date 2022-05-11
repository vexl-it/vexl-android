package cz.cleevio.repository.repository.chat

import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.network.api.ChatApi
import cz.cleevio.network.data.Resource
import cz.cleevio.network.extensions.tryOnline
import cz.cleevio.repository.model.user.User

class ChatRepositoryImpl constructor(
	private val chatApi: ChatApi,
	private val encryptedPreferenceRepository: EncryptedPreferenceRepository
) : ChatRepository {

	override suspend fun sendMessage(): Resource<Unit> = tryOnline(
		request = { chatApi.postMessage("fake_message") },
		mapper = { Unit }
	)

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
}