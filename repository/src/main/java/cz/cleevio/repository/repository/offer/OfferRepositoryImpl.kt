package cz.cleevio.repository.repository.offer

import androidx.sqlite.db.SimpleSQLiteQuery
import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.cache.TransactionProvider
import cz.cleevio.cache.dao.*
import cz.cleevio.cache.entity.ChatUserIdentityEntity
import cz.cleevio.cache.entity.MyOfferEntity
import cz.cleevio.cache.entity.OfferCommonFriendCrossRef
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.network.LocationApi
import cz.cleevio.network.api.OfferApi
import cz.cleevio.network.data.Resource
import cz.cleevio.network.data.Status
import cz.cleevio.network.extensions.tryOnline
import cz.cleevio.network.request.offer.CreateOfferPrivatePartRequest
import cz.cleevio.network.request.offer.CreateOfferRequest
import cz.cleevio.network.request.offer.DeletePrivatePartRequest
import cz.cleevio.network.request.offer.UpdateOfferRequest
import cz.cleevio.repository.RandomUtils
import cz.cleevio.repository.model.chat.fromCache
import cz.cleevio.repository.model.contact.fromDao
import cz.cleevio.repository.model.currency.fromCache
import cz.cleevio.repository.model.offer.*
import cz.cleevio.repository.repository.chat.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.text.Normalizer

class OfferRepositoryImpl constructor(
	private val offerApi: OfferApi,
	private val locationApi: LocationApi,
	private val myOfferDao: MyOfferDao,
	private val offerDao: OfferDao,
	private val requestedOfferDao: RequestedOfferDao,
	private val locationDao: LocationDao,
	private val contactDao: ContactDao,
	private val offerCommonFriendCrossRefDao: OfferCommonFriendCrossRefDao,
	private val transactionProvider: TransactionProvider,
	private val chatRepository: ChatRepository,
	private val cryptoCurrencyDao: CryptoCurrencyDao,
	private val chatUserDao: ChatUserDao,
	private val encryptedPreference: EncryptedPreferenceRepository,
) : OfferRepository {

	override val buyOfferFilter = MutableStateFlow(OfferFilter())

	override val sellOfferFilter = MutableStateFlow(OfferFilter())

	override suspend fun createOffer(offerList: List<NewOffer>, expiration: Long, offerKeys: KeyPair, offerType: String): Resource<Offer> {
		//create offer
		val offerCreateResource = tryOnline(
			request = {
				offerApi.postOffers(
					CreateOfferRequest(
						offerPrivateList = offerList.map { it.toNetwork() },
						expiration = expiration,
						offerType = offerType
					)
				)
			},
			//we need to map to different stuff, so we will instead not map at all
			mapper = { it }
		)

		offerCreateResource.data?.fromNetworkToAdmin()?.let { offer ->
			//save keys and info into my offers
			saveMyOfferIdAndKeys(
				offerId = offer.offerId,
				adminId = offer.adminId,
				privateKey = offerKeys.privateKey,
				publicKey = offerKeys.publicKey,
				offerType = offer.offerType,
				isInboxCreated = false
			)

			//create inbox
			val inboxResponse = chatRepository.createInbox(offerKeys.publicKey)
			when (inboxResponse.status) {
				is Status.Success -> {
					//update info in my offers
					saveMyOfferIdAndKeys(
						offerId = offer.offerId,
						adminId = offer.adminId,
						privateKey = offerKeys.privateKey,
						publicKey = offerKeys.publicKey,
						offerType = offer.offerType,
						isInboxCreated = true
					)
				}
				is Status.Error -> {
					//do we need other flow for errors?
					return Resource.error(inboxResponse.errorIdentification)
				}
				else -> Unit
			}
		}

		offerCreateResource.data?.fromNetwork()?.let { offer ->
			//save offer into DB
			updateOffers(listOf(offer))
		}

		return if (offerCreateResource.status == Status.Success) {
			Resource.success(data = offerCreateResource.data?.fromNetwork())
		} else {
			Resource.error(offerCreateResource.errorIdentification)
		}
	}

	override suspend fun createOfferForPublicKeys(offerId: String, offerList: List<NewOffer>): Resource<Unit> {
		return tryOnline(
			request = {
				offerApi.postOffersPrivatePart(
					CreateOfferPrivatePartRequest(
						privateParts = offerList.map { it.toNetwork() },
						adminId = myOfferDao.getAdminIdByOfferId(offerId)
					)
				)
			},
			mapper = { }
		)
	}

	override suspend fun updateOffer(offerId: String, offerList: List<NewOffer>): Resource<Offer> = tryOnline(
		request = {
			offerApi.putOffers(
				UpdateOfferRequest(
					adminId = myOfferDao.getAdminIdByOfferId(offerId),
					offerPrivateCreateList = offerList.map { it.toNetwork() }
				)
			)
		},
		mapper = { it?.fromNetwork(cryptoCurrencyValues = cryptoCurrencyDao.getCryptoCurrency()?.fromCache()) },
		doOnSuccess = {
			it?.let { offer ->
				updateOffers(listOf(offer))
			}
		}
	)

	override suspend fun loadOffersForMe(): Resource<List<Offer>> = tryOnline(
		request = {
			offerApi.getOffersMe()
		},
		mapper = { it?.items?.map { item -> item.fromNetwork(cryptoCurrencyValues = cryptoCurrencyDao.getCryptoCurrency()?.fromCache()) } }
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

	override suspend fun deleteOfferById(offerId: String): Resource<Unit> = deleteMyOffers(
		listOf(offerId)
	)

	override suspend fun deleteOfferForPublicKeys(deletePrivatePartRequest: DeletePrivatePartRequest)
		: Resource<Unit> = tryOnline(
		request = {
			offerApi.deleteOffersPrivatePart(
				deletePrivatePartRequest = deletePrivatePartRequest.copy(adminIds = deletePrivatePartRequest.adminIds.map { offerId ->
					myOfferDao.getAdminIdByOfferId(offerId)
				})
			)
		},
		mapper = { },
		doOnSuccess = {
			syncOffers()
		}
	)

	//NOT USED
//	override suspend fun refreshOffer(offerId: String): Resource<List<Offer>> = tryOnline(
//		request = { offerApi.getOffersId(listOf(offerId)) },
//		mapper = { items -> items?.map { item -> item.fromNetwork() } ?: emptyList() },
//		doOnSuccess = {
//			it?.let { offers ->
//				updateOffers(offers)
//			}
//		}
//	)

	override suspend fun saveMyOfferIdAndKeys(
		offerId: String,
		adminId: String,
		privateKey: String,
		publicKey: String,
		offerType: String,
		isInboxCreated: Boolean
	): Resource<Unit> {
		myOfferDao.replace(
			MyOfferEntity(
				extId = offerId,
				adminId = adminId,
				privateKey = privateKey,
				publicKey = publicKey,
				offerType = offerType,
				isInboxCreated = isInboxCreated
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
		/* 	FIXME this query is not working if offer price range match second or other nested conditions then is the first one
			It is filtered programatically at the bottom of this method
			Maybe change query from AND/OR to BETWEEN

		if (offerFilter.priceRangeBottomLimit != null && offerFilter.priceRangeTopLimit != null) {
			queryBuilder.append(" AND (")
			queryBuilder.append(" (amountTopLimit >= $SQL_VALUE_PLACEHOLDER AND amountTopLimit <= $SQL_VALUE_PLACEHOLDER)")
			queryBuilder.append(" OR (amountBottomLimit >= $SQL_VALUE_PLACEHOLDER AND amountBottomLimit <= $SQL_VALUE_PLACEHOLDER)")
			queryBuilder.append(" OR ($SQL_VALUE_PLACEHOLDER >= amountBottomLimit AND $SQL_VALUE_PLACEHOLDER <= amountTopLimit)")
			queryBuilder.append(" OR ($SQL_VALUE_PLACEHOLDER >= amountBottomLimit AND $SQL_VALUE_PLACEHOLDER <= amountTopLimit)")
			queryBuilder.append(" )")

			values.add(offerFilter.priceRangeBottomLimit)
			values.add(offerFilter.priceRangeTopLimit)
			values.add(offerFilter.priceRangeBottomLimit)
			values.add(offerFilter.priceRangeTopLimit)
			values.add(offerFilter.priceRangeBottomLimit)
			values.add(offerFilter.priceRangeBottomLimit)
			values.add(offerFilter.priceRangeTopLimit)
			values.add(offerFilter.priceRangeTopLimit)
		}*/

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
		val newOffers = getNewOffers()
		if (newOffers.isSuccess()) overwriteOffers(newOffers.data.orEmpty())
	}

	override suspend fun getMyOffersCount(offerType: String): Int =
		myOfferDao.getMyOfferCount(offerType)

	private suspend fun overwriteOffers(offers: List<Offer>) {
		val myOfferIds = myOfferDao.listAll().map { it.extId }
		val requestedOffersIds = requestedOfferDao.listAll().map { it.offerId }
		transactionProvider.runAsTransaction {
			offerCommonFriendCrossRefDao.clearTable()
			locationDao.clearTable()
			offerDao.clearTable()
			offers.forEach { offer ->
				offer.isMine = myOfferIds.contains(offer.offerId)
				offer.isRequested = requestedOffersIds.contains(offer.offerId)
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
			offerApi.getModifiedOffers(0, Int.MAX_VALUE, "1970-01-01T00:00:00.000Z")
		},
		mapper = { it?.items?.map { item -> item.fromNetwork(cryptoCurrencyValues = cryptoCurrencyDao.getCryptoCurrency()?.fromCache()) } }
	)

	override suspend fun getMyOffersWithoutInbox(): List<MyOffer> =
		myOfferDao.getMyOffersWithoutInbox()
			.map { it.fromCache() }

	override suspend fun getMyOffers(): List<MyOffer> =
		myOfferDao.listAll()
			.map { it.fromCache() }

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
