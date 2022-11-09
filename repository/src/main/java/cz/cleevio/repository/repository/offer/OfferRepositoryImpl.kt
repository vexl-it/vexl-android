package cz.cleevio.repository.repository.offer

import androidx.sqlite.db.SimpleSQLiteQuery
import com.cleevio.vexl.cryptography.model.KeyPair
import com.squareup.moshi.Moshi
import cz.cleevio.cache.TransactionProvider
import cz.cleevio.cache.dao.*
import cz.cleevio.cache.entity.ChatUserIdentityEntity
import cz.cleevio.cache.entity.MyOfferEntity
import cz.cleevio.cache.entity.OfferCommonFriendCrossRef
import cz.cleevio.cache.entity.ReportedOfferEntity
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.network.LocationApi
import cz.cleevio.network.api.OfferApi
import cz.cleevio.network.api.OfferApiV2
import cz.cleevio.network.data.ErrorIdentification
import cz.cleevio.network.data.Resource
import cz.cleevio.network.data.Status
import cz.cleevio.network.extensions.tryOnline
import cz.cleevio.network.request.offer.DeletePrivatePartRequest
import cz.cleevio.network.request.offer.ReportOfferRequest
import cz.cleevio.network.response.offer.v2.CreateOfferPrivatePartRequestV2
import cz.cleevio.network.response.offer.v2.OfferCreateRequestV2
import cz.cleevio.network.response.offer.v2.UpdateOfferRequestV2
import cz.cleevio.repository.R
import cz.cleevio.repository.RandomUtils
import cz.cleevio.repository.model.chat.fromCache
import cz.cleevio.repository.model.contact.fromDao
import cz.cleevio.repository.model.currency.fromCache
import cz.cleevio.repository.model.offer.*
import cz.cleevio.repository.model.offer.v2.NewOfferPrivateV2
import cz.cleevio.repository.model.offer.v2.toNetworkV2
import cz.cleevio.repository.repository.chat.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.text.Normalizer

class OfferRepositoryImpl constructor(
	private val offerApi: OfferApi,
	private val offerApiV2: OfferApiV2,
	private val locationApi: LocationApi,
	private val myOfferDao: MyOfferDao,
	private val offerDao: OfferDao,
	private val requestedOfferDao: RequestedOfferDao,
	private val reportedOfferDao: ReportedOfferDao,
	private val locationDao: LocationDao,
	private val contactDao: ContactDao,
	private val offerCommonFriendCrossRefDao: OfferCommonFriendCrossRefDao,
	private val transactionProvider: TransactionProvider,
	private val chatRepository: ChatRepository,
	private val cryptoCurrencyDao: CryptoCurrencyDao,
	private val chatUserDao: ChatUserDao,
	private val encryptedPreference: EncryptedPreferenceRepository,
	private val moshi: Moshi,
	private val chatMessageDao: ChatMessageDao,
) : OfferRepository {

	override val buyOfferFilter = MutableStateFlow(OfferFilter())

	override val sellOfferFilter = MutableStateFlow(OfferFilter())

	override val isOfferSyncInProgress = MutableStateFlow(false)

	override suspend fun createOffer(
		offerList: List<NewOfferPrivateV2?>,
		expiration: Long,
		offerKeys: KeyPair,
		offerType: String,
		encryptedFor: List<String>,
		payloadPublic: String,
		symmetricalKey: String,
		friendLevel: String,
	): Resource<Offer> {
		//create offer
		val offerCreateResource = tryOnline(
			request = {
				offerApiV2.postOffers(
					OfferCreateRequestV2(
						payloadPublic = payloadPublic,
						offerPrivateList = offerList.filterNotNull().map { it.toNetworkV2() },
						expiration = expiration,
						offerType = offerType
					)
				)
			},
			//we need to map to different stuff, so we will instead not map at all
			mapper = { it }
		)

		offerCreateResource.data?.fromNetworkToAdmin(offerType)?.let { offer ->
			//save keys and info into my offers
			saveMyOfferIdAndKeys(
				offerId = offer.offerId,
				adminId = offer.adminId,
				privateKey = offerKeys.privateKey,
				publicKey = offerKeys.publicKey,
				offerType = offer.offerType,
				isInboxCreated = false,
				encryptedFor = encryptedFor,
				symmetricalKey = symmetricalKey,
				friendLevel = friendLevel
			)

			//create inbox
			val inboxResponse = chatRepository.createInbox(offerKeys.publicKey, offer.offerId)
			when (inboxResponse.status) {
				is Status.Success -> {
					//update info in my offers
					saveMyOfferIdAndKeys(
						offerId = offer.offerId,
						adminId = offer.adminId,
						privateKey = offerKeys.privateKey,
						publicKey = offerKeys.publicKey,
						offerType = offer.offerType,
						isInboxCreated = true,
						encryptedFor = encryptedFor,
						symmetricalKey = symmetricalKey,
						friendLevel = friendLevel
					)
				}
				is Status.Error -> {
					//do we need other flow for errors?
					return Resource.error(inboxResponse.errorIdentification)
				}
				else -> Unit
			}
		}

		offerCreateResource.data?.fromNetwork(
			moshi = moshi,
			keyPair = KeyPair(
				privateKey = encryptedPreference.userPrivateKey,
				publicKey = encryptedPreference.userPublicKey
			)
		)?.let { offer ->
			//save offer into DB
			updateOffers(listOf(offer))
		}

		return if (offerCreateResource.status == Status.Success) {
			Resource.success(
				data = offerCreateResource.data?.fromNetwork(
					moshi = moshi,
					keyPair = KeyPair(
						privateKey = encryptedPreference.userPrivateKey,
						publicKey = encryptedPreference.userPublicKey
					)
				)
			)
		} else {
			Resource.error(offerCreateResource.errorIdentification)
		}
	}

	override suspend fun createOfferForPublicKeys(
		offerId: String,
		offerList: List<NewOfferPrivateV2>,
		additionalEncryptedFor: List<String>
	): Resource<Unit> {
		val adminId = myOfferDao.getAdminIdByOfferId(offerId)
		return if (adminId != null) {
			tryOnline(
				request = {
					offerApiV2.postOffersPrivatePart(
						CreateOfferPrivatePartRequestV2(
							offerPrivateList = offerList.map { it.toNetworkV2() },
							adminId = adminId
						)
					)
				},
				mapper = { },
				doOnSuccess = { _ ->
					//update `encryptedFor` field in myOfferDao with contactsPublicKeys
					val nullableMyOffer = myOfferDao.getMyOfferById(offerId)?.fromCache()
					nullableMyOffer?.let { myOffer ->
						val tempEncryptedFor = myOffer.encryptedFor.toMutableSet()
						tempEncryptedFor.addAll(
							additionalEncryptedFor
						)
						myOfferDao.replace(
							myOffer.copy(
								encryptedFor = tempEncryptedFor.filter { it.isNotBlank() }.toList()
							).toCache()
						)
					}
				}
			)
		} else {
			Resource.error(ErrorIdentification.MessageError(message = R.string.error_offer_not_found))
		}
	}

	override suspend fun updateOffer(
		offerId: String,
		offerList: List<NewOfferPrivateV2>,
		additionalEncryptedFor: List<String>,
		payloadPublic: String,
	): Resource<Offer> {
		val adminId = myOfferDao.getAdminIdByOfferId(offerId)
		return if (adminId != null) {
			tryOnline(
				request = {
					offerApiV2.putOffers(
						UpdateOfferRequestV2(
							adminId = adminId,
							payloadPublic = payloadPublic,
							offerPrivateList = offerList.map { it.toNetworkV2() }
						)
					)
				},
				mapper = {
					it?.fromNetwork(
						moshi = moshi,
						cryptoCurrencyValues = cryptoCurrencyDao.getCryptoCurrency()?.fromCache(),
						reportedOfferIds = reportedOfferDao.listAllIds(),
						keyPair = KeyPair(
							privateKey = encryptedPreference.userPrivateKey,
							publicKey = encryptedPreference.userPublicKey
						)
					)
				},
				doOnSuccess = { nullableOffer ->
					//update `encryptedFor` field in myOfferDao with contactsPublicKeys
					val nullableMyOffer = myOfferDao.getMyOfferById(offerId)?.fromCache()
					nullableMyOffer?.let { myOffer ->
						val tempEncryptedFor = myOffer.encryptedFor.toMutableSet()
						tempEncryptedFor.addAll(
							additionalEncryptedFor
						)
						myOfferDao.replace(
							myOffer.copy(
								encryptedFor = tempEncryptedFor.filter { it.isNotBlank() }.toList()
							).toCache()
						)
					}

					nullableOffer?.let { offer ->
						updateOffers(listOf(offer))
					}
				}
			)
		} else {
			Resource.error(ErrorIdentification.MessageError(message = R.string.error_offer_not_found))
		}
	}

	override suspend fun loadOffersForMe(): Resource<List<Offer>> = tryOnline(
		request = {
			offerApiV2.getOffersMe()
		},
		mapper = {
			val reportedOfferIds = reportedOfferDao.listAllIds()
			it?.offers?.mapNotNull { item ->
				item.fromNetwork(
					moshi = moshi,
					cryptoCurrencyValues = cryptoCurrencyDao.getCryptoCurrency()?.fromCache(),
					reportedOfferIds = reportedOfferIds,
					keyPair = KeyPair(
						privateKey = encryptedPreference.userPrivateKey,
						publicKey = encryptedPreference.userPublicKey
					)
				)
			}
		}
	)

	override suspend fun deleteMyOffers(offerIds: List<String>): Resource<Unit> = tryOnline(
		request = {
			offerApi.deleteOffersId(adminIds = offerIds.map { offerId ->
				myOfferDao.getAdminIdByOfferId(offerId)
			})
		},
		mapper = { },
		doOnSuccess = {
			offerDao.deleteOffersById(offerIds)
			myOfferDao.deleteMyOffersById(offerIds)
		}
	)

	override suspend fun deleteMyOfferById(offerId: String): Resource<Unit> = deleteMyOffers(
		listOf(offerId)
	)

	override suspend fun deleteBrokenMyOffersFromDB(offerIds: List<String>) {
		myOfferDao.deleteMyOffersById(offerIds)
	}

	override suspend fun deleteOfferForPublicKeys(
		deletePrivatePartRequest: DeletePrivatePartRequest,
	): Resource<Unit> {
		return tryOnline(
			request = {
				offerApi.deleteOffersPrivatePart(
					deletePrivatePartRequest = deletePrivatePartRequest
				)
			},
			mapper = { },
			doOnSuccess = { _ ->
				//update `encryptedFor` for all my offers
				deletePrivatePartRequest.adminIds.map { offerId ->
					val nullableMyOffer = myOfferDao.getMyOfferById(offerId)?.fromCache()
					nullableMyOffer?.let { myOffer ->
						val tempEncryptedFor = myOffer.encryptedFor.toMutableSet()
						tempEncryptedFor.removeAll(
							deletePrivatePartRequest.publicKeys.toSet()
						)
						myOfferDao.replace(
							myOffer.copy(
								encryptedFor = tempEncryptedFor.filter { it.isNotBlank() }.toList()
							).toCache()
						)
					}
				}

				syncOffers()
			}
		)
	}

	override suspend fun saveMyOfferIdAndKeys(
		offerId: String,
		adminId: String,
		privateKey: String,
		publicKey: String,
		offerType: String,
		isInboxCreated: Boolean,
		encryptedFor: List<String>,
		symmetricalKey: String,
		friendLevel: String
	): Resource<Unit> {
		myOfferDao.replace(
			MyOfferEntity(
				extId = offerId,
				adminId = adminId,
				privateKey = privateKey,
				publicKey = publicKey,
				offerType = offerType,
				isInboxCreated = isInboxCreated,
				encryptedForKeys = encryptedFor.joinToString(),
				symmetricalKey = symmetricalKey,
				friendLevel = friendLevel,
				version = MY_OFFER_VERSION
			)
		)
		return Resource.success(data = Unit)
	}

	override suspend fun loadOfferKeysByOfferId(offerId: String): KeyPair? {
		val entity = myOfferDao.getMyOfferById(offerId)
		return entity?.let {
			KeyPair(privateKey = entity.privateKey, publicKey = entity.publicKey)
		}
	}

	override suspend fun loadSymmetricalKeyByOfferId(offerId: String): String? {
		val entity = myOfferDao.getMyOfferById(offerId)
		return entity?.let {
			entity.symmetricalKey
		}
	}

	override suspend fun getOffers() =
		offerDao.getAllExtendedOffers().map {
			it.offer.fromCache(it.locations, it.commonFriends, chatUserDao)
		}

	override fun getOffersFlow() = offerDao.getAllExtendedOffersFlow().map { list ->
		list.map {
			it.offer.fromCache(it.locations, it.commonFriends, chatUserDao)
		}
	}

	override fun getFilteredAndSortedOffersByTypeFlow(
		offerTypeName: String,
		offerFilter: OfferFilter
	): Flow<List<Offer>> {
		val queryBuilder = StringBuilder("")
		val values = arrayListOf<Any>()

		queryBuilder.append("SELECT * FROM OfferEntity WHERE offerType == $SQL_VALUE_PLACEHOLDER")
		values.add(offerTypeName)
		queryBuilder.append(" AND active == $SQL_VALUE_PLACEHOLDER")
		values.add(true)
		queryBuilder.append(" AND isMine == $SQL_VALUE_PLACEHOLDER")
		values.add(false)

		if (offerFilter.locationTypes?.isNotEmpty() == true) {
			val types = offerFilter.locationTypes.joinToString(
				separator = SQL_VALUE_SEPARATOR,
				transform = { SQL_VALUE_PLACEHOLDER }
			)
			queryBuilder.append(" AND locationState in($types)")
			values.addAll(offerFilter.locationTypes)
		}
		if (offerFilter.paymentMethods?.isNotEmpty() == true) {
			queryBuilder.append(
				sqlLikeOperator(
					fieldName = "paymentMethod",
					data = offerFilter.paymentMethods.toList()
				)
			)
		}
		if (offerFilter.btcNetworks?.isNotEmpty() == true) {
			queryBuilder.append(
				sqlLikeOperator(
					fieldName = "btcNetwork",
					data = offerFilter.btcNetworks.toList()
				)
			)
		}
		if (offerFilter.friendLevels?.isNotEmpty() == true) {
			val levels = offerFilter.friendLevels.joinToString(
				separator = SQL_VALUE_SEPARATOR,
				transform = { SQL_VALUE_PLACEHOLDER }
			)
			queryBuilder.append(" AND friendLevel in($levels)")
			values.addAll(offerFilter.friendLevels)
		}
		if (offerFilter.feeTypes?.isNotEmpty() == true) {
			val types = offerFilter.feeTypes.joinToString(
				separator = SQL_VALUE_SEPARATOR,
				transform = { SQL_VALUE_PLACEHOLDER }
			)

			if (offerFilter.feeValue != null) {
				queryBuilder.append(" AND (feeState in($types) OR feeAmount <= $SQL_VALUE_PLACEHOLDER)")
				values.addAll(offerFilter.feeTypes)
				values.add(offerFilter.feeValue)
			} else {
				queryBuilder.append(" AND feeState in($types)")
				values.addAll(offerFilter.feeTypes)
			}
		}
		if (offerFilter.currency != null) {
			queryBuilder.append(" AND currency == $SQL_VALUE_PLACEHOLDER")
			values.add(offerFilter.currency)
		}
		if (offerFilter.groupUuids?.isNotEmpty() == true) {
			queryBuilder.append(
				sqlLikeOperator(
					fieldName = "groupUuid",
					data = offerFilter.groupUuids.toList()
				)
			)
		}

		queryBuilder.append(" ORDER BY createdAt DESC, isRequested ASC")
		val simpleSQLiteQuery = SimpleSQLiteQuery(
			queryBuilder.toString(),
			values.toTypedArray()
		)

		return offerDao.getFilteredOffersFlow(simpleSQLiteQuery).map { list ->
			list.map {
				it.offer.fromCache(it.locations, it.commonFriends, chatUserDao)
			}.filter { offer ->
				offerFilter.isOfferMatchPriceRange(offer.amountBottomLimit.toFloat(), offer.amountTopLimit.toFloat())
					&& offerFilter.isOfferLocationInRadius(offer.location)
			}
		}
	}

	override fun getOffersSortedByDateOfCreationFlow(offerTypeName: String): Flow<List<Offer>> {
		val queryBuilder = StringBuilder("")
		val values = arrayListOf<Any>()

		queryBuilder.append("SELECT * FROM OfferEntity ORDER BY createdAt DESC, isRequested ASC")

		val simpleSQLiteQuery = SimpleSQLiteQuery(
			queryBuilder.toString(),
			values.toTypedArray()
		)

		return offerDao.getFilteredOffersFlow(simpleSQLiteQuery).map { list ->
			list.map {
				it.offer.fromCache(it.locations, it.commonFriends, chatUserDao)
			}
		}.map { list ->
			list.filter { it.offerType == offerTypeName && it.isMine }
		}
	}

	override suspend fun syncOffers() {
		isOfferSyncInProgress.emit(true)
		val newOffers = getNewOffers()
		if (newOffers.isSuccess()) overwriteOffers(newOffers.data.orEmpty())
		isOfferSyncInProgress.emit(false)
	}

	override suspend fun getMyActiveOffersCount(offerType: String): Int {
		return offerDao.getMyActiveOfferCount(offerType)
	}

	private suspend fun overwriteOffers(offers: List<Offer>) {
		val myOfferIds = myOfferDao.listAll().map { it.extId }
		val requestedOffersPublicKeys = chatMessageDao.getAllRequestedInboxPublicKeys(
			encryptedPreference.userPublicKey
		)
		transactionProvider.runAsTransaction {
			offerCommonFriendCrossRefDao.clearTable()
			locationDao.clearTable()
			offerDao.clearTable()
			offers.forEach { offer ->
				offer.isMine = myOfferIds.contains(offer.offerId)
				offer.isRequested = requestedOffersPublicKeys.contains(offer.offerPublicKey)
				val offerId = offerDao.insertOffer(offer.toCache())
				locationDao.insertLocations(offer.location.map {
					it.toCache(offerId)
				})
				val commonFriendRefs = contactDao.getAllContacts()
					.map { it.fromDao() }
					.filter { contact ->
						offer.commonFriends.firstOrNull { it.contactHash == contact.getHashedContact() } != null
					}
					.map {
						OfferCommonFriendCrossRef(
							offerId = offerId,
							contactId = it.id
						)
					}
				offerCommonFriendCrossRefDao.replaceAll(commonFriendRefs)

				if (!offer.isMine) {
					//try to find saved user name and photo and stuff
					val chatUserIdentity = chatUserDao.getUserByContactKey(offer.offerPublicKey)?.fromCache()
					//if not found
					if (chatUserIdentity == null) {
						//we need to create ChatUser for this offer
						val entity = ChatUserIdentityEntity(
							contactPublicKey = offer.offerPublicKey,
							inboxKey = encryptedPreference.userPublicKey,
							anonymousUsername = RandomUtils.generateName(),
							anonymousAvatarImageIndex = RandomUtils.getAvatarIndex(),
							deAnonymized = false
						)
						//save it into DB
						chatUserDao.replace(entity)
					}
				}
			}
		}
	}

	private suspend fun updateOffers(offers: List<Offer>) {
		val myOfferIds = myOfferDao.listAll().map { it.extId }
		transactionProvider.runAsTransaction {
			offers.forEach { offer ->
				offer.isMine = myOfferIds.contains(offer.offerId)
				val offerId = offerDao.insertOffer(offer.toCache())
				locationDao.insertLocations(offer.location.map {
					it.toCache(offerId)
				})
				// because we don't have full objects at this moment yet (it's from the API, not database)
				val commonFriendRefs = contactDao.getAllContacts()
					.map { it.fromDao() }
					.filter { contact ->
						offer.commonFriends.firstOrNull { it.contactHash == contact.getHashedContact() } != null
					}
					.map {
						OfferCommonFriendCrossRef(
							offerId = offerId,
							contactId = it.id
						)
					}
				offerCommonFriendCrossRefDao.replaceAll(commonFriendRefs)
			}
		}
	}

	private suspend fun getNewOffers(): Resource<List<Offer>> = tryOnline(
		request = {
			offerApiV2.getModifiedOffers("1970-01-01T00:00:00.000Z")
		},
		mapper = {
			val reportedOfferIds = reportedOfferDao.listAllIds()
			it?.offers?.mapNotNull { item ->
				item.fromNetwork(
					moshi = moshi,
					cryptoCurrencyValues = cryptoCurrencyDao.getCryptoCurrency()?.fromCache(),
					reportedOfferIds = reportedOfferIds,
					keyPair = KeyPair(
						privateKey = encryptedPreference.userPrivateKey,
						publicKey = encryptedPreference.userPublicKey
					)
				)
			}
		}
	)

	override suspend fun getMyOffersWithoutInbox(): List<MyOffer> =
		myOfferDao.getMyOffersWithoutInbox()
			.map { it.fromCache() }

	override suspend fun getMyOffers(version: Long?): List<MyOffer> {
		return when (version) {
			null -> {
				myOfferDao.listAll()
					.map { it.fromCache() }
			}
			else -> {
				myOfferDao.listByVersion(version)
					.map { it.fromCache() }
			}
		}
	}


	override suspend fun getLocationSuggestions(
		count: Int, query: String, language: String
	): Resource<List<LocationSuggestion>> = tryOnline(
		request = {
			locationApi.getSuggestions(count, query, language)
		},
		mapper = {
			it?.result?.map { a ->
				LocationSuggestion(
					city = a.userData.municipality,
					region = a.userData.region,
					country = a.userData.country,
					latitude = BigDecimal(a.userData.latitude),
					longitude = BigDecimal(a.userData.longitude),
					suggestFirstRow = a.userData.suggestFirstRow,
					suggestSecondRow = a.userData.suggestSecondRow,
				)
			}
				?.filter { a -> a.cityText.isNotBlank() }
				?.distinctBy { a -> a.cityText }
				?.filter { a -> a.cityText.removeNonSpacingMarks().contains(query.removeNonSpacingMarks(), ignoreCase = true) }
				.orEmpty()
		}
	)

	override suspend fun clearOfferTables() {
		requestedOfferDao.clearTable()
		reportedOfferDao.clearTable()
		offerDao.clearTable()
		offerCommonFriendCrossRefDao.clearTable()
		myOfferDao.clearTable()
		locationDao.clearTable()
		contactDao.clearTable()
	}

	override suspend fun getOfferById(offerId: String): Offer? {
		return offerDao.getOfferById(offerId = offerId).let {
			it?.offer?.fromCache(it.locations, it.commonFriends, chatUserDao)
		}
	}

	override suspend fun reportOffer(offerId: String): Resource<Unit> {
		val response = tryOnline(
			request = {
				offerApi.postOffersReport(
					reportOfferRequest = ReportOfferRequest(offerId = offerId)
				)
			},
			mapper = { }
		)

		reportedOfferDao.insert(
			ReportedOfferEntity(offerId = offerId)
		)

		//delete offer from local DB, it will be disabled on next offers sync
		offerDao.deleteOffersById(listOf(offerId))

		return response
	}

	override suspend fun forceReEncrypt() {
		//get all and change
		val cleared = myOfferDao.listAll().map { it.copy(encryptedForKeys = "") }
		myOfferDao.replaceAll(cleared)
	}

	private fun sqlLikeOperator(fieldName: String, data: List<Any>): String {
		val query = StringBuilder()
		query.append(" AND (")
		data.forEachIndexed { index, paymentMethod ->
			query.append("$fieldName LIKE '%$paymentMethod%'")
			if (data.lastIndex != index) {
				query.append(" OR ")
			}
		}
		query.append(")")
		return query.toString()
	}

	companion object {
		private const val SQL_VALUE_SEPARATOR = ","
		private const val SQL_VALUE_PLACEHOLDER = "?"
	}

	fun String.removeNonSpacingMarks() =
		Normalizer.normalize(this, Normalizer.Form.NFD)
			.replace("\\p{Mn}+".toRegex(), "")
}
