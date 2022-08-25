package cz.cleevio.repository.repository.chat

import com.cleevio.vexl.cryptography.EcdsaCryptoLib
import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.cache.dao.*
import cz.cleevio.cache.entity.ChatUserIdentityEntity
import cz.cleevio.cache.entity.MessageKeyPair
import cz.cleevio.cache.entity.NotificationEntity
import cz.cleevio.cache.entity.RequestedOfferEntity
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.network.api.ChatApi
import cz.cleevio.network.api.ContactApi
import cz.cleevio.network.data.ErrorIdentification
import cz.cleevio.network.data.Resource
import cz.cleevio.network.data.Status
import cz.cleevio.network.extensions.tryOnline
import cz.cleevio.network.request.chat.*
import cz.cleevio.network.request.contact.FirebaseTokenUpdateRequest
import cz.cleevio.repository.R
import cz.cleevio.repository.model.chat.*
import cz.cleevio.repository.model.contact.CommonFriend
import cz.cleevio.repository.model.group.fromEntity
import cz.cleevio.repository.model.offer.fromCache
import cz.cleevio.repository.model.offer.fromCacheWithoutFriendsMapping
import cz.cleevio.repository.repository.UsernameUtils
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
	private val groupDao: GroupDao
) : ChatRepository {

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

		return null
	}

	override fun getMessages(inboxPublicKey: String, firstKey: String, secondKey: String): SharedFlow<List<ChatMessage>> =
		chatMessageDao.listAllBySenders(inboxPublicKey = inboxPublicKey, firstKey = firstKey, secondKey = secondKey)
			.map { messages -> messages.map { singleMessage -> singleMessage.fromCache() } }
			.shareIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, replay = 1)

	@Suppress("ReturnCount")
	override suspend fun saveFirebasePushToken(token: String) {
		//save token into DB on error
		notificationDao.replace(NotificationEntity(token = token, uploaded = false))

		val inboxKeys = getMyInboxKeys()

		//update firebase token for each of them
		//todo: ask BE for EP where we sent list of publicKeys
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

	override suspend fun createInbox(publicKey: String): Resource<Unit> {
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
					mapper = { }
				)
			} ?: Resource.error(ErrorIdentification.MessageError(message = R.string.error_missing_challenge))
		} ?: Resource.error(ErrorIdentification.MessageError(message = R.string.error_missing_firebase_token))
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
						messages
							?.filter { it.type == MessageType.DELETE_CHAT }
							?.forEach { deleteMessage ->
								//delete all messages from and for this user
								chatMessageDao.deleteByKeys(
									inboxPublicKey = deleteMessage.inboxPublicKey,
									firstKey = deleteMessage.senderPublicKey,
									secondKey = deleteMessage.recipientPublicKey
								)
							}

						//special handling for APPROVE_MESSAGING - create anonymized user for the other user
						messages
							?.filter {
								it.type == MessageType.APPROVE_MESSAGING
							}?.forEach { message ->
								// create anonymous identity
								chatUserDao.replace(
									ChatUserIdentityEntity(
										contactPublicKey = message.senderPublicKey, // sender's key, because it's incoming message
										inboxKey = message.inboxPublicKey,
										// Fixme: correctly linked avatar index and name is missing
										// Fixme: For the avatar index look into eg. - OfferFriendLevelWidget
										anonymousUsername = UsernameUtils.generateName(),
										anonymousAvatarImageIndex = null,
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
										avatar = user.image
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
				avatar = message.deAnonImage
			)
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
		//we don't want to store DELETE_CHAT message, delete other messages instead
		if (message.type == MessageType.DELETE_CHAT) {
			chatMessageDao.deleteByKeys(
				inboxPublicKey = message.inboxPublicKey,
				firstKey = message.senderPublicKey,
				secondKey = message.recipientPublicKey
			)
		} else {
			//we save every message into DB before upload to BE
			//todo: should we add some `uploaded` flag?
			chatMessageDao.insert(
				message.toCache()
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
				mapper = { }
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
	): Resource<Unit> {
		chatMessageDao.insert(
			message.toCache()
		)
		return tryOnline(
			request = {
				chatApi.postInboxesApprovalRequest(
					ApprovalRequest(
						publicKey = publicKey,
						message = message.toNetwork(publicKey)
					)
				)
			},
			mapper = { },
			doOnSuccess = {
				requestedOfferDao.replace(RequestedOfferEntity(offerId = offerId))
			}
		)
	}

	override suspend fun confirmCommunicationRequest(
		offerId: String,
		publicKeyToConfirm: String,
		message: ChatMessage,
		originalRequestMessage: ChatMessage,
		approve: Boolean
	): Resource<Unit> {
		val myOfferKeyPair = myOfferDao.getOfferKeysByExtId(offerId)
		val senderKeyPair = KeyPair(
			privateKey = myOfferKeyPair.privateKey,
			publicKey = myOfferKeyPair.publicKey
		)

		return refreshChallenge(senderKeyPair)?.let { signatureChallenge ->
			chatMessageDao.insert(
				message.toCache()
			)
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
				mapper = { },
				doOnSuccess = {
					chatMessageDao.replace(originalRequestMessage.copy(isProcessed = true).toCache())
					// create anonymous identity
					chatUserDao.replace(
						ChatUserIdentityEntity(
							contactPublicKey = message.recipientPublicKey, // recipient's key, because of it's outgoing message
							inboxKey = message.inboxPublicKey,
							anonymousUsername = UsernameUtils.generateName(),
							deAnonymized = false
						)
					)
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
					val offer = offerWithLocation.offer.fromCacheWithoutFriendsMapping(offerWithLocation.locations, commonFriends)
					result.add(
						CommunicationRequest(
							message = message,
							offer = offer,
							group = groupDao.getOneByUuid(offer.groupUuid)?.fromEntity()
						)
					)
				}
			}
		}

		return result.toList()
	}

	override suspend fun loadChatUsers(): List<ChatListUser> {
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
						it.offer.fromCache(it.locations, it.commonFriends)
					}.firstOrNull()

					if (offer != null) {
						val originalChatListUser = ChatListUser(
							message = latestMessage,
							offer = offer,
							user = chatUserDao.getUserIdentity(latestMessage.inboxPublicKey, contactPublicKey)?.fromCache()
						)

						if (originalChatListUser.offer.isMine) {
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
		}

		return result.toList()
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
				it.offer.fromCache(it.locations, it.commonFriends)
			}.firstOrNull()

			if (offer != null) {
				val originalChatListUser = ChatListUser(
					message = latestMessage,
					offer = offer,
					user = chatUserDao.getUserIdentity(latestMessage.inboxPublicKey, contactPublicKey)?.fromCache()
				)

				return if (originalChatListUser.offer.isMine) {
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
		}

		return null
	}

	override suspend fun clearChatTables() {
		chatUserDao.clearTable()
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
		chatMessageDao.delete(communicationRequest.message.toCache())
	}

	override suspend fun getKeyPairByMyPublicKey(myPublicKey: String): KeyPair? {
		if (myPublicKey == encryptedPreferenceRepository.userPublicKey) {
			return KeyPair(
				privateKey = encryptedPreferenceRepository.userPrivateKey,
				publicKey = encryptedPreferenceRepository.userPublicKey
			)
		}

		return myOfferDao.getMyOfferByPublicKey(myPublicKey)?.let {
			KeyPair(
				privateKey = it.privateKey,
				publicKey = it.publicKey
			)
		}
	}
}