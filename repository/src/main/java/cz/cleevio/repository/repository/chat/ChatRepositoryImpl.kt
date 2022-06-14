package cz.cleevio.repository.repository.chat

import com.cleevio.vexl.cryptography.EcdsaCryptoLib
import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.cache.dao.ChatMessageDao
import cz.cleevio.cache.dao.NotificationDao
import cz.cleevio.cache.entity.NotificationEntity
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.network.api.ChatApi
import cz.cleevio.network.data.ErrorIdentification
import cz.cleevio.network.data.Resource
import cz.cleevio.network.data.Status
import cz.cleevio.network.extensions.tryOnline
import cz.cleevio.network.request.chat.CreateChallengeRequest
import cz.cleevio.network.request.chat.CreateInboxRequest
import cz.cleevio.network.request.chat.MessageRequest
import cz.cleevio.network.request.chat.SendMessageRequest
import cz.cleevio.repository.R
import cz.cleevio.repository.model.chat.*
import cz.cleevio.repository.model.user.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import timber.log.Timber

class ChatRepositoryImpl constructor(
	private val chatApi: ChatApi,
	private val notificationDao: NotificationDao,
	private val chatMessageDao: ChatMessageDao,
	private val encryptedPreferenceRepository: EncryptedPreferenceRepository
) : ChatRepository {

	//map PublicKey -> Signature
	private val signatures = mutableMapOf<String, ChatChallenge>()

	private fun hasSignature(publicKey: String): Boolean = getSignature(publicKey) != null

	private fun getSignature(publicKey: String): String? {
		return signatures[publicKey]?.let {
			if (it.expiration < System.currentTimeMillis()) {
				null
			} else {
				it.signature
			}
		}
	}

	private suspend fun refreshChallenge(keyPair: KeyPair): Resource<Unit> {
		val challenge = tryOnline(
			request = { chatApi.postChallenge(challengeRequest = CreateChallengeRequest(keyPair.publicKey)) },
			mapper = { it?.fromNetwork() }
		)
		//solve challenge
		return when (challenge.status) {
			is Status.Success -> {
				challenge.data?.let { chatChallenge ->
					val signature = EcdsaCryptoLib.sign(
						keyPair, chatChallenge.challenge
					)
					//save to map
					signatures.put(keyPair.publicKey, chatChallenge.copy(signature = signature))
				}
				Resource.success(Unit)
			}
			is Status.Error -> {
				//fixme: give proper error message
				Resource.error(ErrorIdentification.MessageError(message = R.string.error_unknown_error_occurred))
			}
			else -> {
				Resource.error(ErrorIdentification.MessageError(message = R.string.error_unknown_error_occurred))
			}
		}
	}

	override fun getMessages(inboxPublicKey: String, senderPublicKeys: List<String>): SharedFlow<List<ChatMessage>> =
		chatMessageDao.listAllBySenders(inboxPublicKey = inboxPublicKey, senderPublicKeys = senderPublicKeys)
			.map { messages -> messages.map { singleMessage -> singleMessage.fromCache() } }
			.shareIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, replay = 1)

	//todo: add also some mechanism to update all existing inboxes with new firebase token
	override suspend fun saveFirebasePushToken(token: String) {
		//save token into DB on error
		notificationDao.replace(NotificationEntity(token = token))
	}

	override suspend fun createInbox(publicKey: String): Resource<Unit> {
		return notificationDao.getOne()?.let { notificationDataStorage ->
			tryOnline(
				request = {
					chatApi.postInboxes(
						inboxRequest = CreateInboxRequest(
							publicKey = publicKey, token = notificationDataStorage.token
						)
					)
				},
				mapper = { }
			)
		} ?: Resource.error(ErrorIdentification.MessageError(code = R.string.error_missing_firebase_token))
	}

	override suspend fun syncMessages(keyPair: KeyPair): Resource<Unit> {
		//verify that you have valid challenge for this inbox
		if (!hasSignature(keyPair.publicKey)) {
			//refresh challenge
			refreshChallenge(keyPair)
		}

		val signatureNullable = getSignature(keyPair.publicKey)
		signatureNullable?.let { signature ->
			//load messages
			val messagesResponse = tryOnline(
				request = {
					chatApi.putInboxesMessages(
						messageRequest = MessageRequest(
							publicKey = keyPair.publicKey,
							signature = signature
						)
					)
				},
				mapper = { it?.messages?.map { message -> message.fromNetwork(keyPair.publicKey) } },
				//save messages to DB
				doOnSuccess = { messages ->
					chatMessageDao.insertAll(
						messages?.map { it.toCache() } ?: listOf()
					)
				}
			)
			when (messagesResponse.status) {
				is Status.Error -> {
					return Resource.error(messagesResponse.errorIdentification)
				}
				else -> {

				}
			}

			//todo: add correct text
		} ?: return Resource.error(ErrorIdentification.MessageError(message = R.string.error_unknown_error_occurred))

		//delete messages from BE
		val deleteResponse = deleteMessagesFromBE(keyPair.publicKey)
		when (deleteResponse.status) {
			is Status.Error -> {
				return Resource.error(deleteResponse.errorIdentification)
			}
			else -> {

			}
		}

		return Resource.success(Unit)
	}

	override suspend fun sendMessage(senderPublicKey: String, receiverPublicKey: String, message: String, messageType: String): Resource<Unit> = tryOnline(
		request = {
			chatApi.postInboxesMessages(
				sendMessageRequest = SendMessageRequest(
					senderPublicKey = senderPublicKey, receiverPublicKey = receiverPublicKey, message = message, messageType = messageType
				)
			)
		},
		mapper = { }
	)

	override suspend fun deleteMessagesFromBE(publicKey: String): Resource<Unit> = tryOnline(
		request = { chatApi.deleteInboxesMessages(publicKey) },
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
					publicKey = "xxY"
				),
				User(
					id = 157,
					extId = 102,
					username = "Unknown Friend 3",
					avatar = "url",
					publicKey = "xxzz"
				)
			)
		)
	}
}