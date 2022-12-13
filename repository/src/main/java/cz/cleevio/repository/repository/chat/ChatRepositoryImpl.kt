package cz.cleevio.repository.repository.chat

import com.cleevio.vexl.cryptography.EcdsaCryptoLib
import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.cache.dao.*
import cz.cleevio.cache.entity.*
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.network.api.ChatApi
import cz.cleevio.network.api.ContactApi
import cz.cleevio.network.data.ErrorIdentification
import cz.cleevio.network.data.Resource
import cz.cleevio.network.data.Status
import cz.cleevio.network.extensions.tryOnline
import cz.cleevio.network.request.chat.*
import cz.cleevio.network.request.contact.FirebaseTokenUpdateRequest
import cz.cleevio.network.response.chat.MessageResponse
import cz.cleevio.network.response.chat.MessagesResponse
import cz.cleevio.repository.R
import cz.cleevio.repository.RandomUtils
import cz.cleevio.repository.model.chat.*
import cz.cleevio.repository.model.contact.CommonFriend
import cz.cleevio.repository.model.group.fromEntity
import cz.cleevio.repository.model.offer.fromCache
import cz.cleevio.repository.model.offer.fromCacheWithoutFriendsMapping
import cz.cleevio.repository.repository.contact.ContactRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatRepositoryImpl constructor(
	private val chatApi: ChatApi,
	private val contactApi: ContactApi,
	private val notificationDao: NotificationDao,
	private val chatMessageDao: ChatMessageDao,
	private val chatUserDao: ChatUserDao,
	private val myOfferDao: MyOfferDao,
	private val requestedOfferDao: RequestedOfferDao,
	private val offerDao: OfferDao,
	private val userDao: UserDao,
	private val encryptedPreferenceRepository: EncryptedPreferenceRepository,
	private val contactRepository: ContactRepository,
	private val groupDao: GroupDao,
	private val inboxDao: InboxDao,
) : ChatRepository {

	override val chatUsersFlow: MutableSharedFlow<List<ChatListUser>> = MutableSharedFlow()
	override var chatUsers: List<ChatListUser> = emptyList()

	private suspend fun refreshChallenge(keyPair: KeyPair): SignedChallengeRequest? {
		val challenge = tryOnline(
			request = { chatApi.postChallenge(challengeRequest = CreateChallengeRequest(keyPair.publicKey)) },
			mapper = { it?.fromNetwork() }
		)
		//solve challenge
		return when (challenge.status) {
			is Status.Success -> {
				return challenge.data?.let { chatChallenge ->
					val signature = EcdsaCryptoLib.sign(
						keyPair, chatChallenge.challenge
					)

					//return solved challenge
					SignedChallengeRequest(
						challenge = chatChallenge.challenge,
						signature = signature
					)
				}
			}
			else -> {
				//challenge failed somehow
				null
			}
		}
	}

	private suspend fun refreshChallengeBatch(keyPairs: List<KeyPair>): List<SignedChallengeBatchRequest>? {
		val response = tryOnline(
			request = { chatApi.postChallengeBatch(challengeRequest = CreateChallengeBatchRequest(keyPairs.map { it.publicKey })) },
			mapper = { it }
		)
		//solve challenge
		return when (response.status) {
			is Status.Success -> {
				return response.data?.let { chatChallenges ->
					val signedChallenges = mutableListOf<SignedChallengeBatchRequest>()

					chatChallenges.challenges.forEach { signedChallenge ->
						val keyPair = getKeyPairByMyPublicKey(signedChallenge.publicKey) ?: return@forEach

						val signature = EcdsaCryptoLib.sign(
							keyPair, signedChallenge.challenge
						)

						signedChallenges.add(
							SignedChallengeBatchRequest(
								publicKey = signedChallenge.publicKey,
								signedChallenge = SignedChallengeRequest(
									challenge = signedChallenge.challenge,
									signature = signature
								)
							)
						)
					}

					signedChallenges
				}
			}
			else -> {
				//challenge failed somehow
				null
			}
		}
	}

	override suspend fun getMyInboxKeys(): List<String> {
		//get all offer inbox keys
		val offerKeys = myOfferDao.getAllOfferKeys().map { it.publicKey }.toMutableSet()

		//get all inbox keys -- should be same as offer keys, is there for keeping chats live even after deleting offers
		val inboxKeys = inboxDao.getAllInboxes().map { it.publicKey }.toMutableSet()

		//add all keys together
		inboxKeys.addAll(offerKeys)

		//add users inbox
		inboxKeys.add(
			encryptedPreferenceRepository.userPublicKey
		)

		return inboxKeys.filter {
			it.isNotBlank()
		}.toList()
	}

	private suspend fun findKeyPairByPublicKey(publicKey: String): KeyPair? {
		if (encryptedPreferenceRepository.userPublicKey == publicKey) {
			return KeyPair(
				publicKey = publicKey,
				privateKey = encryptedPreferenceRepository.userPrivateKey
			)
		}

		myOfferDao.getMyOfferByPublicKey(publicKey)?.let {
			return KeyPair(
				publicKey = publicKey,
				privateKey = it.privateKey
			)
		}

		inboxDao.getInboxByPublicKey(publicKey)?.let {
			return KeyPair(
				publicKey = publicKey,
				privateKey = it.privateKey
			)
		}

		return null
	}

	override fun getMessages(inboxPublicKey: String, firstKey: String, secondKey: String): SharedFlow<List<ChatMessage>> =
		chatMessageDao.listAllBySenders(inboxPublicKey = inboxPublicKey, firstKey = firstKey, secondKey = secondKey)
			.map { messages -> messages.map { singleMessage -> singleMessage.fromCache() } }
			.shareIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, replay = 1)

	//TODO: call almost this on app start
	@Suppress("ReturnCount")
	override suspend fun saveFirebasePushToken(token: String) {
		//save token into DB on error
		notificationDao.replace(NotificationEntity(token = token, uploaded = false))

		val inboxKeys = getMyInboxKeys()

		//update firebase token for each of them
		inboxKeys.forEach { inboxKey ->
			val keyPair = getKeyPairByMyPublicKey(inboxKey) ?: return@forEach
			val signedChallenge = refreshChallenge(keyPair) ?: return@forEach
			val response = tryOnline(
				request = {
					chatApi.putInboxes(
						UpdateInboxRequest(
							publicKey = inboxKey,
							token = token,
							signedChallenge = signedChallenge
						)
					)
				},
				mapper = { it }
			)
			if (response.status is Status.Error) {
				//exit on error
				return
			}
		}

		//user token in contact ms
		//do only if we are already onboarded
		if (userDao.getUser()?.finishedOnboarding == true) {
			val user = tryOnline(
				request = {
					contactApi.putUsers(
						firebaseTokenUpdateRequest = FirebaseTokenUpdateRequest(firebaseToken = token)
					)
				},
				mapper = { it }
			)
			if (user.status is Status.Error) {
				//exit on error
				return
			}

			//facebook user token in contact ms
			val facebookUser = tryOnline(
				request = {
					contactApi.putUsers(
						firebaseTokenUpdateRequest = FirebaseTokenUpdateRequest(firebaseToken = token)
					)
				},
				mapper = { it }
			)
			if (facebookUser.status is Status.Error) {
				//exit on error
				return
			}
		}

		//in case of no error, mark firebase token as uploaded
		notificationDao.replace(NotificationEntity(token = token, uploaded = true))
	}

	override suspend fun createInbox(publicKey: String, offerId: String?): Resource<Unit> {
		val keyPair = findKeyPairByPublicKey(publicKey)
			?: return Resource.error(ErrorIdentification.MessageError(message = R.string.error_missing_keypair))
		return notificationDao.getOne()?.let { notificationDataStorage ->
			refreshChallenge(keyPair)?.let { signedChallenge ->
				tryOnline(
					request = {
						chatApi.postInboxes(
							inboxRequest = CreateInboxRequest(
								publicKey = keyPair.publicKey,
								token = notificationDataStorage.token,
								signedChallenge = signedChallenge
							)
						)
					},
					mapper = { },
					doOnSuccess = {
						//create inbox in local DB
						val inboxEntity = InboxEntity(
							inboxType = getInboxType(offerId).name,
							publicKey = keyPair.publicKey,
							privateKey = keyPair.privateKey,
							offerId = offerId
						)
						inboxDao.replace(inboxEntity)
					}
				)
			} ?: Resource.error(ErrorIdentification.MessageError(message = R.string.error_missing_challenge))
		} ?: Resource.error(ErrorIdentification.MessageError(message = R.string.error_missing_firebase_token))
	}

	private fun getInboxType(offerId: String?): InboxType {
		return when {
			offerId != null -> {
				InboxType.OFFER
			}
			offerId == null -> {
				InboxType.PERSONAL
			}
			else -> {
				InboxType.UNKNOWN
			}
		}
	}

	override suspend fun syncMessages(inboxPublicKey: String): Resource<Unit> {
		val keyPair = findKeyPairByPublicKey(inboxPublicKey)
		return keyPair?.let {
			syncMessages(keyPair)
		} ?: Resource.error(ErrorIdentification.MessageError(message = R.string.error_missing_keypair))
	}

	@Suppress("ReturnCount")
	override suspend fun syncMessages(keyPair: KeyPair): Resource<Unit> {
		refreshChallenge(keyPair)?.let { signedChallenge ->
			val messagesResponse = tryOnline(
				request = {
					chatApi.putInboxesMessages(
						messageRequest = MessageRequest(
							publicKey = keyPair.publicKey,
							signedChallenge = signedChallenge
						)
					)
				},
				mapper = { it?.messages?.map { message -> message.fromNetwork(keyPair) } },
				//save messages to DB
				doOnSuccess = { messages ->
					CoroutineScope(Dispatchers.IO).launch {
						messages?.map { it.toCache() }?.let {
							chatMessageDao.replaceAll(it)
						}

						//special handling for DELETE_CHAT
//						messages
//							?.filter { it.type == MessageType.DELETE_CHAT }
//							?.forEach { deleteMessage ->
//								//delete all messages from and for this user
//								chatMessageDao.deleteByKeys(
//									inboxPublicKey = deleteMessage.inboxPublicKey,
//									firstKey = deleteMessage.senderPublicKey,
//									secondKey = deleteMessage.recipientPublicKey
//								)
//							}

						//special handling for REQUEST_MESSAGING - create anonymized user for the other user
						messages
							?.filter {
								it.type == MessageType.REQUEST_MESSAGING
							}?.forEach { message ->
								// create anonymous identity
								chatUserDao.replace(
									ChatUserIdentityEntity(
										contactPublicKey = message.senderPublicKey, // sender's key, because it's incoming message
										inboxKey = message.inboxPublicKey,
										anonymousUsername = RandomUtils.generateName(),
										anonymousAvatarImageIndex = RandomUtils.getAvatarIndex(),
										deAnonymized = false
									)
								)
							}

						//special handling for APPROVE_REVEAL - deanonymize the other user AND solve pending requests
						messages
							?.filter {
								it.type == MessageType.APPROVE_REVEAL
							}?.forEach { message ->
								message.deanonymizedUser?.let { user ->
									chatUserDao.deAnonymizeUser(
										contactPublicKey = message.senderPublicKey, // sender's key, because it's incoming message
										inboxKey = message.inboxPublicKey,
										name = user.name!!, // There has to be some name
										avatarBase64 = user.imageBase64
									)
									chatMessageDao.solvePendingIdentityRevealsBySenders(
										inboxPublicKey = message.inboxPublicKey,
										firstKey = message.senderPublicKey,
										secondKey = message.recipientPublicKey
									)
								}
							}
						//special handling for DISAPPROVE_REVEAL - do nothing, just solve the requests
						messages
							?.filter {
								it.type == MessageType.DISAPPROVE_REVEAL
							}?.forEach { message ->
								chatMessageDao.solvePendingIdentityRevealsBySenders(
									inboxPublicKey = message.inboxPublicKey,
									firstKey = message.senderPublicKey,
									secondKey = message.recipientPublicKey
								)
							}
					}
				}
			)
			if (messagesResponse.status is Status.Error) {
				return Resource.error(messagesResponse.errorIdentification)
			}
		} ?: return Resource.error(ErrorIdentification.MessageError(message = R.string.error_missing_challenge))

		//delete messages from BE
		val deleteResponse = deleteMessagesFromBE(keyPair.publicKey)
		return if (deleteResponse.status is Status.Error) {
			Resource.error(deleteResponse.errorIdentification)
		} else {
			Resource.success(Unit)
		}
	}

	override suspend fun deAnonymizeUser(contactPublicKey: String, inboxKey: String, myPublicKey: String) {
		val lastRequestRevealMessage = chatMessageDao.getLastRequestRevealMessageByUser(
			inboxPublicKey = inboxKey,
			userPublicKey = contactPublicKey,
			myPublicKey = myPublicKey
		)

		lastRequestRevealMessage?.let { message ->
			chatUserDao.deAnonymizeUser(
				contactPublicKey = contactPublicKey,
				inboxKey = inboxKey,
				name = message.deAnonName!!,
				avatarBase64 = message.deAnonImageBase64
			)
		}
	}

	override suspend fun syncAllMessages(): Resource<Unit> {
		val keyPairList = mutableSetOf(
			KeyPair(
				privateKey = encryptedPreferenceRepository.userPrivateKey,
				publicKey = encryptedPreferenceRepository.userPublicKey
			)
		)

		val inboxKeys = inboxDao.getAllInboxes().map {
			KeyPair(
				privateKey = it.privateKey,
				publicKey = it.publicKey
			)
		}
		keyPairList.addAll(
			inboxKeys
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

		val results = keyPairList.map {
			syncMessages(it)
		}

		return results.firstOrNull { it.isError() } ?: Resource.success(Unit)
	}

	@Suppress("ReturnCount")
	override suspend fun sendMessage(
		senderPublicKey: String,
		receiverPublicKey: String,
		message: ChatMessage,
		messageType: String
	): Resource<MessageResponse> {
		//we don't want to store DELETE_CHAT message, delete other messages instead
		if (message.type == MessageType.DELETE_CHAT) {
			chatMessageDao.deleteByKeys(
				inboxPublicKey = message.inboxPublicKey,
				firstKey = message.senderPublicKey,
				secondKey = message.recipientPublicKey
			)
		}

		val keyPair = findKeyPairByPublicKey(senderPublicKey)
			?: return Resource.error(ErrorIdentification.MessageError(message = R.string.error_missing_keypair))

		refreshChallenge(keyPair)?.let { signedChallenge ->
			return tryOnline(
				request = {
					chatApi.postInboxesMessages(
						sendMessageRequest = SendMessageRequest(
							senderPublicKey = senderPublicKey,
							receiverPublicKey = receiverPublicKey,
							message = message.toNetwork(receiverPublicKey),
							messageType = messageType,
							signedChallenge = signedChallenge
						)
					)
				},
				doOnSuccess = {
					if (message.type != MessageType.DELETE_CHAT) {
						val messageWithId = it?.let {
							message.copy(id = it.id)
						} ?: message

						chatMessageDao.replace(messageWithId.toCache())
					}
				},
				mapper = { it }
			)
		} ?: return Resource.error(ErrorIdentification.MessageError(message = R.string.error_missing_challenge))
	}

	@Suppress("ReturnCount")
	override suspend fun sendMessageBatch(
		messages: Map<String, List<ChatMessage>>, // sender public key, Value
		inboxKeys: List<String>
	): Resource<MessagesResponse> {

		//we don't want to store DELETE_CHAT message, delete other messages instead
		messages.values.forEach {
			it.forEach {
				if (it.type == MessageType.DELETE_CHAT) {
					chatMessageDao.deleteByKeys(
						inboxPublicKey = it.inboxPublicKey,
						firstKey = it.senderPublicKey,
						secondKey = it.recipientPublicKey
					)
				}
			}
		}

		val keyPairs = inboxKeys.map { getKeyPairByMyPublicKey(it) }

		refreshChallengeBatch(keyPairs.filterNotNull())?.let { signedChallenge ->

			val messagesList = mutableListOf<SendMessageBatchRequest>()

			signedChallenge.forEach { challenge ->
				val message = SendMessageBatchRequest(
					senderPublicKey = challenge.publicKey,
					messages = messages[challenge.publicKey]?.map {
						BatchMessage(
							it.recipientPublicKey,
							message = it.toNetwork(it.recipientPublicKey),
							it.type.name
						)
					} ?: emptyList(),
					challenge.signedChallenge
				)

				messagesList.add(message)
			}

			return tryOnline(
				request = {
					chatApi.postInboxesMessageBatch(
						sendMessageBatchRequestList = SendMessageBatchRequestList(
							data = messagesList
						)
					)
				},
				doOnSuccess = { messageResponse ->
					/*
					messageResponse?.messages?.forEach {
						if (it.messageType != MessageType.DELETE_CHAT.name) {
							val messageWithId = it?.let {
								message.copy(id = it.id)
							} ?: message

								it.copy(id = it.id)
							chatMessageDao.replace(messageWithId.toCache())
						}
					}
					 */
				},
				mapper = { it }
			)
		} ?: return Resource.error(ErrorIdentification.MessageError(message = R.string.error_missing_challenge))
	}

	override suspend fun processMessage(message: ChatMessage) {
		chatMessageDao.replace(
			message.copy(isProcessed = true).toCache()
		)
	}

	override suspend fun deleteMessagesFromBE(publicKey: String): Resource<Unit> {
		val keyPair = findKeyPairByPublicKey(publicKey)
			?: return Resource.error(ErrorIdentification.MessageError(message = R.string.error_missing_keypair))
		return refreshChallenge(keyPair)?.let { signedChallenge ->
			tryOnline(
				request = {
					chatApi.deleteInboxesMessages(
						DeletionRequest(
							publicKey = publicKey,
							signedChallenge = signedChallenge
						)
					)
				},
				mapper = { }
			)
		} ?: Resource.error(ErrorIdentification.MessageError(message = R.string.error_missing_keypair))
	}

	override suspend fun changeUserBlock(
		senderKeyPair: KeyPair, publicKeyToBlock: String,
		block: Boolean
	): Resource<Unit> {
		return refreshChallenge(senderKeyPair)?.let { signedChallenge ->
			tryOnline(
				request = {
					chatApi.putInboxesBlock(
						BlockInboxRequest(
							publicKey = senderKeyPair.publicKey,
							publicKeyToBlock = publicKeyToBlock,
							signedChallenge = signedChallenge,
							block = block
						)
					)
				},
				mapper = { }
			)
		} ?: Resource.error(ErrorIdentification.MessageError(message = R.string.error_missing_challenge))
	}

	override suspend fun askForCommunicationApproval(
		publicKey: String,
		offerId: String,
		message: ChatMessage
	): Resource<MessageResponse> {
		return tryOnline(
			request = {
				chatApi.postInboxesApprovalRequest(
					ApprovalRequest(
						publicKey = publicKey,
						message = message.toNetwork(publicKey)
					)
				)
			},
			mapper = { it },
			doOnSuccess = {
				val messageWithId = it?.let {
					message.copy(id = it.id)
				} ?: message

				chatMessageDao.insert(messageWithId.toCache())

				requestedOfferDao.replace(RequestedOfferEntity(offerId = offerId))
				//trigger
				val offer = offerDao.getOfferById(offerId = offerId)
				offer?.let {
					offerDao.insertOffer(it.offer.copy(isRequested = true))
				}
			}
		)
	}

	override suspend fun confirmCommunicationRequest(
		offerId: String,
		publicKeyToConfirm: String,
		message: ChatMessage,
		originalRequestMessage: ChatMessage,
		approve: Boolean
	): Resource<MessageResponse> {
		val myOfferKeyPair = myOfferDao.getOfferKeysByExtId(offerId)
		val senderKeyPair = KeyPair(
			privateKey = myOfferKeyPair.privateKey,
			publicKey = myOfferKeyPair.publicKey
		)

		return refreshChallenge(senderKeyPair)?.let { signatureChallenge ->
			val confirmResponse = tryOnline(
				request = {
					chatApi.postInboxesApprovalConfirm(
						ApprovalConfirmRequest(
							publicKey = senderKeyPair.publicKey,
							publicKeyToConfirm = publicKeyToConfirm,
							signedChallenge = signatureChallenge,
							message = message.toNetwork(publicKeyToConfirm),
							approve = approve
						)
					)
				},
				mapper = { it },
				doOnSuccess = {
					val messageWithId = it?.let {
						message.copy(id = it.id)
					} ?: message

					//save your confirm/deny message into DB
					chatMessageDao.replace(messageWithId.toCache())
					//mark original REQUEST_MESSAGING message as processed
					chatMessageDao.replace(originalRequestMessage.copy(isProcessed = true).toCache())
				}
			)

			confirmResponse
		} ?: Resource.error(ErrorIdentification.MessageError(message = R.string.error_missing_challenge))
	}

	override suspend fun deleteInbox(publicKey: String): Resource<Unit> {
		val keyPair = getKeyPairByMyPublicKey(publicKey) ?: return Resource.error(ErrorIdentification.MessageError(message = R.string.error_unknown_error_occurred))

		return refreshChallenge(keyPair)?.let { signatureChallenge ->
			tryOnline(
				request = {
					chatApi.deleteInboxes(
						DeletionRequest(
							publicKey = publicKey,
							signedChallenge = signatureChallenge
						)
					)
				},
				mapper = { }
			)
		} ?: Resource.error(ErrorIdentification.MessageError(message = R.string.error_missing_challenge))
	}

	override suspend fun deleteAllInboxes(publicKeys: List<String>): Resource<Unit> {
		val keyPairs = publicKeys.map { getKeyPairByMyPublicKey(it) }

		return refreshChallengeBatch(keyPairs.filterNotNull())?.let { signatureChallenge ->
			tryOnline(
				request = {
					chatApi.deleteInboxesBatch(
						DeletionBatchRequest(
							dataForRemoval = signatureChallenge.map {
								DeletionRequest(
									it.publicKey,
									it.signedChallenge
								)
							}
						)
					)
				},
				mapper = { }
			)
		} ?: Resource.error(ErrorIdentification.MessageError(message = R.string.error_missing_challenge))
	}

	override suspend fun loadCommunicationRequests(): List<CommunicationRequest> {
		val result: MutableList<CommunicationRequest> = mutableListOf()
		//get all communication requests
		val communicationRequests = chatMessageDao.listAllPendingCommunicationMessages()
			.map { it.fromCache() }

		communicationRequests.forEach { message ->
			//inboxPublicKey in communication requests should be always offer public key, because users react to offer
			val offerPublicKey = message.inboxPublicKey
			//find offerId by public key
			val myOfferId = myOfferDao.getMyOfferByPublicKey(offerPublicKey)?.extId
			myOfferId?.let { offerId ->
				//find offer by offerId
				val offerWithLocation = offerDao.getOfferById(offerId)

				val myOfferCommonFriends = contactRepository.getCommonFriends(listOf(message.senderPublicKey))

				val commonFriends = myOfferCommonFriends[message.senderPublicKey].orEmpty().map {
					CommonFriend(
						it.getHashedContact(),
						it
					)
				}

				// Let it there
				if (offerWithLocation != null) {
					val offer = offerWithLocation.offer.fromCacheWithoutFriendsMapping(
						offerWithLocation.locations,
						commonFriends,
						chatUserDao,
						chatUserKey = message.senderPublicKey,
						inboxKey = message.inboxPublicKey
					)
					val contactLevels = contactRepository.getContactKeysByPublicKey(message.senderPublicKey).map { it.level }
					result.add(
						CommunicationRequest(
							message = message,
							offer = offer,
							group = groupDao.getOneByUuid(offer.groupUuids.firstOrNull() ?: "")?.fromEntity(),
							contactLevels = contactLevels
						)
					)
				}
			}
		}

		return result.toList()
	}

	override suspend fun startEmittingChatUsers() {
		//emit default value
		loadChatUsers()
		//and emit again whenever there is new MessageEntity
		chatMessageDao.listAllFlow().collectLatest { loadChatUsers() }
	}

	private suspend fun loadChatUsers() {
		val result: MutableList<ChatListUser> = mutableListOf()
		//get keys to all of my inboxes
		val inboxKeys = getMyInboxKeys()
		inboxKeys.forEach { inboxKey ->
			//go over all inboxes, find all messages, sort by time, take X latest?
			val contactMessageKeyPairs = chatMessageDao.getAllContactKeys(inboxKey)
			val contactMessageKeysWithoutDuplicities = emptyList<MessageKeyPair>().toMutableList()
			contactMessageKeyPairs.forEach {
				// remove duplicities (where sender is recipient, and recipient is sender)
				val redundant = MessageKeyPair(
					senderPublicKey = it.recipientPublicKey,
					recipientPublicKey = it.senderPublicKey
				)
				if (!contactMessageKeysWithoutDuplicities.contains(redundant)) {
					contactMessageKeysWithoutDuplicities.add(it)
				}
			}
			contactMessageKeysWithoutDuplicities.forEach { contactMessageKeyPair ->
				val latestMessage = chatMessageDao.getLatestBySenders(
					inboxPublicKey = inboxKey,
					firstKey = contactMessageKeyPair.senderPublicKey,
					secondKey = contactMessageKeyPair.recipientPublicKey
				)?.fromCache()

				if (latestMessage != null) {
					val contactPublicKey = if (latestMessage.isMine) {
						latestMessage.recipientPublicKey
					} else {
						latestMessage.senderPublicKey
					}

					val offer = offerDao.getAllExtendedOffers().filter {
						it.offer.offerPublicKey == latestMessage.recipientPublicKey ||
							it.offer.offerPublicKey == latestMessage.senderPublicKey
					}.map {
						it.offer.fromCache(it.locations, it.commonFriends, chatUserDao)
					}.firstOrNull()

					val originalChatListUser = ChatListUser(
						message = latestMessage,
						offer = offer,
						user = chatUserDao.getUserIdentity(latestMessage.inboxPublicKey, contactPublicKey)?.fromCache()
					)

					if (originalChatListUser.offer?.isMine == true) {
						// For our own offer we don't have common friends, because when creating the offer we don't
						// know who will contact us, and every conversation will have different common friends.
						// So here we don't read that data from database, but ask API directly
						val myOfferCommonFriends = contactRepository.getCommonFriends(listOf(contactPublicKey))
						result.add(
							originalChatListUser.copy(
								offer = originalChatListUser.offer.copy(
									commonFriends = myOfferCommonFriends[contactPublicKey].orEmpty().map {
										CommonFriend(
											it.getHashedContact(),
											it
										)
									}
								)
							)
						)
					} else {
						result.add(originalChatListUser)
					}
				}
			}
		}

		chatUsersFlow.emit(result.toList())
		chatUsers = result.toList()
	}

	override suspend fun getOneChatUser(messageKeyPair: MessageKeyPair): ChatListUser? {
		val latestMessage = chatMessageDao.getLatestBySenders(
			inboxPublicKey = messageKeyPair.recipientPublicKey,
			firstKey = messageKeyPair.senderPublicKey,
			secondKey = messageKeyPair.recipientPublicKey
		)?.fromCache()

		if (latestMessage != null) {
			val contactPublicKey = if (latestMessage.isMine) {
				latestMessage.recipientPublicKey
			} else {
				latestMessage.senderPublicKey
			}
			val offer = offerDao.getAllExtendedOffers().filter {
				it.offer.offerPublicKey == latestMessage.recipientPublicKey ||
					it.offer.offerPublicKey == latestMessage.senderPublicKey
			}.map {
				it.offer.fromCache(it.locations, it.commonFriends, chatUserDao)
			}.firstOrNull()

			val originalChatListUser = ChatListUser(
				message = latestMessage,
				offer = offer,
				user = chatUserDao.getUserIdentity(latestMessage.inboxPublicKey, contactPublicKey)?.fromCache()
			)

			return if (originalChatListUser.offer?.isMine == true) {
				// For our own offer we don't have common friends, because when creating the offer we don't
				// know who will contact us, and every conversation will have different common friends.
				// So here we don't read that data from database, but ask API directly
				val myOfferCommonFriends = contactRepository.getCommonFriends(listOf(contactPublicKey))
				originalChatListUser.copy(
					offer = originalChatListUser.offer.copy(
						commonFriends = myOfferCommonFriends[contactPublicKey].orEmpty().map {
							CommonFriend(
								it.getHashedContact(),
								it
							)
						}
					)
				)
			} else {
				originalChatListUser
			}
		}

		return null
	}

	override suspend fun clearChatTables() {
		chatUserDao.clearTable()
		inboxDao.clearTable()
		chatMessageDao.deleteAll()
	}

	override fun getChatUserIdentityFlow(inboxKey: String, contactPublicKey: String): Flow<ChatUserIdentity?> {
		return chatUserDao.getUserIdentityFlow(inboxKey, contactPublicKey).map {
			it?.fromCache()
		}
	}

	override fun getPendingIdentityRequest(
		inboxPublicKey: String,
		firstKey: String,
		secondKey: String
	): Flow<Boolean> {
		return chatMessageDao.listPendingIdentityRevealsBySenders(
			inboxPublicKey = inboxPublicKey,
			firstKey = firstKey,
			secondKey = secondKey
		).map {
			it.isNotEmpty()
		}
	}

	override fun hasPendingDeleteChatRequest(
		inboxPublicKey: String,
		firstKey: String,
		secondKey: String
	): Flow<Boolean> {
		return chatMessageDao.listPendingDeleteChatBySendersFlow(
			inboxPublicKey = inboxPublicKey,
			firstKey = firstKey,
			secondKey = secondKey
		).map {
			it.isNotEmpty()
		}
	}

	override fun getPendingDeleteChatRequest(
		inboxPublicKey: String,
		firstKey: String,
		secondKey: String
	): List<ChatMessage> {
		return chatMessageDao.listPendingDeleteChatBySenders(
			inboxPublicKey = inboxPublicKey,
			firstKey = firstKey,
			secondKey = secondKey
		).map {
			it.fromCache()
		}
	}

	override fun canRequestIdentity(
		inboxPublicKey: String,
		firstKey: String,
		secondKey: String
	): Flow<Boolean> {
		return chatMessageDao.listPendingAndApprovedIdentityReveals(
			inboxPublicKey = inboxPublicKey,
			firstKey = firstKey,
			secondKey = secondKey
		).map {
			it.isEmpty()
		}
	}

	override fun solveIdentityRevealRequest(
		inboxPublicKey: String,
		firstKey: String,
		secondKey: String
	) {
		chatMessageDao.solvePendingIdentityRevealsBySenders(
			inboxPublicKey = inboxPublicKey,
			firstKey = firstKey,
			secondKey = secondKey
		)
	}

	override suspend fun deleteMessage(communicationRequest: CommunicationRequest) {
		communicationRequest.message?.toCache()?.let {
			chatMessageDao.delete(it)
		}
	}

	override suspend fun getKeyPairByMyPublicKey(myPublicKey: String): KeyPair? {
		if (myPublicKey == encryptedPreferenceRepository.userPublicKey) {
			return KeyPair(
				privateKey = encryptedPreferenceRepository.userPrivateKey,
				publicKey = encryptedPreferenceRepository.userPublicKey
			)
		}

		myOfferDao.getMyOfferByPublicKey(myPublicKey)?.let {
			return KeyPair(
				privateKey = it.privateKey,
				publicKey = it.publicKey
			)
		}

		inboxDao.getInboxByPublicKey(myPublicKey)?.let {
			return return KeyPair(
				publicKey = myPublicKey,
				privateKey = it.privateKey
			)
		}

		return null
	}

	override suspend fun createInboxesForMeAndOffers() {
		//personal inbox
		if (userDao.getUser()?.finishedOnboarding == true) {
			val personalInbox = InboxEntity(
				inboxType = InboxType.PERSONAL.name,
				publicKey = encryptedPreferenceRepository.userPublicKey,
				privateKey = encryptedPreferenceRepository.userPrivateKey,
				offerId = null
			)
			inboxDao.replace(personalInbox)
		}

		//offer inboxes
		val myOffers = myOfferDao.listAll()
		myOffers.forEach { myOffer ->
			val inboxEntity = InboxEntity(
				inboxType = InboxType.OFFER.name,
				publicKey = myOffer.publicKey,
				privateKey = myOffer.privateKey,
				offerId = myOffer.extId
			)
			inboxDao.replace(inboxEntity)
		}

		encryptedPreferenceRepository.hasCreatedInternalInboxesForOffers = true
	}

	override suspend fun deleteChat(deleteMessage: ChatMessage) {
		//extra check for correct message type, could work with any message type
		if (deleteMessage.type == MessageType.DELETE_CHAT) {
			//delete all messages from and for this user
			chatMessageDao.deleteByKeys(
				inboxPublicKey = deleteMessage.inboxPublicKey,
				firstKey = deleteMessage.senderPublicKey,
				secondKey = deleteMessage.recipientPublicKey
			)
		}
	}
}