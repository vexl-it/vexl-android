package cz.cleevio.repository.repository.chat

import com.cleevio.vexl.cryptography.EcdsaCryptoLib
import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.cache.dao.ChatMessageDao
import cz.cleevio.cache.dao.MyOfferDao
import cz.cleevio.cache.dao.NotificationDao
import cz.cleevio.cache.dao.OfferDao
import cz.cleevio.cache.entity.NotificationEntity
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.network.api.ChatApi
import cz.cleevio.network.data.ErrorIdentification
import cz.cleevio.network.data.Resource
import cz.cleevio.network.data.Status
import cz.cleevio.network.extensions.tryOnline
import cz.cleevio.network.request.chat.*
import cz.cleevio.repository.R
import cz.cleevio.repository.model.chat.*
import cz.cleevio.repository.model.offer.fromCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class ChatRepositoryImpl constructor(
	private val chatApi: ChatApi,
	private val notificationDao: NotificationDao,
	private val chatMessageDao: ChatMessageDao,
	private val myOfferDao: MyOfferDao,
	private val offerDao: OfferDao,
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

	override suspend fun getMyInboxKeys(): List<String> {
		//get all offer inbox keys
		val inboxKeys = myOfferDao.getAllOfferPublicKeys().toMutableList()
		//add users inbox
		inboxKeys.add(
			encryptedPreferenceRepository.userPublicKey
		)

		return inboxKeys.filter {
			it.isNotBlank()
		}.toList()
	}

	override fun getMessages(inboxPublicKey: String, senderPublicKeys: List<String>): SharedFlow<List<ChatMessage>> =
		chatMessageDao.listAllBySenders(inboxPublicKey = inboxPublicKey, senderPublicKeys = senderPublicKeys)
			.map { messages -> messages.map { singleMessage -> singleMessage.fromCache() } }
			.shareIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, replay = 1)

	override suspend fun saveFirebasePushToken(token: String) {
		//save token into DB on error
		notificationDao.replace(NotificationEntity(token = token, uploaded = false))

		val inboxKeys = getMyInboxKeys()

		//update firebase token for each of them
		//todo: ask BE for EP where we sent list of publicKeys
		inboxKeys.forEach { inboxKey ->
			val response = tryOnline(
				request = {
					chatApi.putInboxes(
						UpdateInboxRequest(publicKey = inboxKey, token = token)
					)
				},
				mapper = { it }
			)
			if (response.status is Status.Error) {
				//exit on error
				return
			}
		}

		//in case of no error, mark firebase token as uploaded
		notificationDao.replace(NotificationEntity(token = token, uploaded = true))
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
		} ?: Resource.error(ErrorIdentification.MessageError(message = R.string.error_missing_firebase_token))
	}

	@Suppress("ReturnCount")
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
			if (messagesResponse.status is Status.Error) {
				return Resource.error(messagesResponse.errorIdentification)
			}
			//todo: add correct text
		} ?: return Resource.error(ErrorIdentification.MessageError(message = R.string.error_unknown_error_occurred))

		//delete messages from BE
		val deleteResponse = deleteMessagesFromBE(keyPair.publicKey)
		return if (deleteResponse.status is Status.Error) {
			Resource.error(deleteResponse.errorIdentification)
		} else {
			Resource.success(Unit)
		}
	}

	override suspend fun syncAllMessages(): Resource<Unit> {
		val keyPairList = mutableListOf(
			KeyPair(
				privateKey = encryptedPreferenceRepository.userPrivateKey,
				publicKey = encryptedPreferenceRepository.userPublicKey
			)
		)

		val keyPairs = myOfferDao.getAllOfferKeys()
		keyPairList.addAll(
			keyPairs.map {
				KeyPair(
					privateKey = it.privateKey,
					publicKey = it.publicKey
				)
			}
		)

		keyPairList.forEach {
			val result = syncMessages(it)
		}

		return Resource.success(Unit) // TODO think about the result status
	}

	@Suppress("ReturnCount")
	override suspend fun sendMessage(
		senderPublicKey: String, receiverPublicKey: String,
		message: ChatMessage, messageType: String
	): Resource<Unit> {
		//todo: should we add some `uploaded` flag?
		chatMessageDao.insert(
			message.toCache()
		)
		return tryOnline(
			request = {
				chatApi.postInboxesMessages(
					sendMessageRequest = SendMessageRequest(
						senderPublicKey = senderPublicKey, receiverPublicKey = receiverPublicKey,
						message = message.toNetwork(), messageType = messageType
					)
				)
			},
			mapper = { }
		)
	}

	override suspend fun deleteMessagesFromBE(publicKey: String): Resource<Unit> = tryOnline(
		request = { chatApi.deleteInboxesMessages(publicKey) },
		mapper = { }
	)

	override suspend fun changeUserBlock(
		senderKeyPair: KeyPair, publicKeyToBlock: String,
		block: Boolean
	): Resource<Unit> {
		if (!hasSignature(senderKeyPair.publicKey)) {
			//refresh challenge
			refreshChallenge(senderKeyPair)
		}

		val signatureNullable = getSignature(senderKeyPair.publicKey)
		return signatureNullable?.let { signature ->
			val blockResponse = tryOnline(
				request = {
					chatApi.putInboxesBlock(
						BlockInboxRequest(
							publicKey = senderKeyPair.publicKey,
							publicKeyToBlock = publicKeyToBlock,
							signature = signature,
							block = block
						)
					)
				},
				mapper = { }
			)
			blockResponse
			//todo: add correct text
		} ?: Resource.error(ErrorIdentification.MessageError(message = R.string.error_unknown_error_occurred))
	}

	override suspend fun askForCommunicationApproval(publicKey: String, message: ChatMessage): Resource<Unit> {
		return tryOnline(
			request = {
				chatApi.postInboxesApprovalRequest(
					ApprovalRequest(
						publicKey = publicKey,
						message = message.toNetwork()
					)
				)
			},
			mapper = { }
		)
	}

	override suspend fun confirmCommunicationRequest(
		offerId: String, publicKeyToConfirm: String,
		message: ChatMessage, approve: Boolean
	): Resource<Unit> {
		val myOfferKeyPair = myOfferDao.getOfferKeysByExtId(offerId)
		val senderKeyPair = KeyPair(
			privateKey = myOfferKeyPair.privateKey,
			publicKey = myOfferKeyPair.publicKey
		)

		if (!hasSignature(senderKeyPair.publicKey)) {
			//refresh challenge
			refreshChallenge(senderKeyPair)
		}

		val signatureNullable = getSignature(senderKeyPair.publicKey)
		return signatureNullable?.let { signature ->
			val confirmResponse = tryOnline(
				request = {
					chatApi.postInboxesApprovalConfirm(
						ApprovalConfirmRequest(
							publicKey = senderKeyPair.publicKey,
							publicKeyToConfirm = publicKeyToConfirm,
							signature = signature,
							message = message.toNetwork(),
							approve = approve
						)
					)
				},
				mapper = { }
			)

			confirmResponse
		} ?: Resource.error(ErrorIdentification.MessageError(message = R.string.error_unknown_error_occurred))
	}

	override suspend fun deleteInbox(publicKey: String): Resource<Unit> = tryOnline(
		request = {
			chatApi.deleteInboxes(publicKey = publicKey)
		},
		mapper = { }
	)

	override suspend fun loadCommunicationRequests(): List<CommunicationRequest> {
		val result: MutableList<CommunicationRequest> = mutableListOf()
		//get all communication requests
		val communicationRequests = chatMessageDao.listAllMessagesByType(MessageType.COMMUNICATION_REQUEST.name)
			.map { it.fromCache() }

		communicationRequests.forEach { message ->
			//inboxPublicKey in communication requests should be always offer public key, because users react to offer
			val offerPublicKey = message.inboxPublicKey
			//find offerId by public key
			val myOfferId = myOfferDao.getMyOfferByPublicKey(offerPublicKey)?.extId
			myOfferId?.let { offerId ->
				//find offer by offerId
				val offerWithLocation = offerDao.getOfferById(offerId)
				val offer = offerWithLocation.offer.fromCache(offerWithLocation.locations)
				result.add(
					CommunicationRequest(
						message = message,
						offer = offer
					)
				)
			}
		}

		return result.toList()
	}

	override suspend fun loadChatUsers(): List<ChatListUser> {
		val result: MutableList<ChatListUser> = mutableListOf()
		//go over all inboxes, find all messages, sort by time, take X latest?
		val contactKeys = chatMessageDao.getAllContactKeys()
		//get keys to all of my inboxes
		val inboxKeys = getMyInboxKeys()
		inboxKeys.forEach { inboxKey ->
			contactKeys.forEach { contactPublicKey ->
				val latestMessage = chatMessageDao.getLatestBySenders(
					inboxPublicKey = inboxKey,
					senderPublicKeys = listOf(inboxKey, contactPublicKey)
				)?.fromCache()

				if (latestMessage != null && latestMessage.type != MessageType.COMMUNICATION_REQUEST) {
					//todo: check if we know user's identity. Maybe go over all messages from this user
					// and look for type ANON_REQUEST_RESPONSE?
					result.add(
						ChatListUser(
							message = latestMessage,
							offer = offerDao.getAllOffers().filter {
								it.offerPublicKey == latestMessage.recipientPublicKey || it.offerPublicKey == latestMessage.senderPublicKey
							}.map {
								it.fromCache(emptyList())
							}.firstOrNull()
						)
					)
				}
			}
		}

		return result.toList()
	}

	override suspend fun deleteMessage(communicationRequest: CommunicationRequest) {
		chatMessageDao.delete(communicationRequest.message.toCache())
	}
}