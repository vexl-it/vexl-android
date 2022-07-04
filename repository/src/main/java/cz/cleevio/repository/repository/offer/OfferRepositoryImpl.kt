package cz.cleevio.repository.repository.offer

import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.cache.TransactionProvider
import cz.cleevio.cache.dao.*
import cz.cleevio.cache.entity.MyOfferEntity
import cz.cleevio.cache.entity.OfferCommonFriendCrossRef
import cz.cleevio.network.api.OfferApi
import cz.cleevio.network.data.Resource
import cz.cleevio.network.data.Status
import cz.cleevio.network.extensions.tryOnline
import cz.cleevio.network.request.offer.CreateOfferRequest
import cz.cleevio.network.request.offer.UpdateOfferRequest
import cz.cleevio.repository.model.offer.*
import kotlinx.coroutines.flow.map

class OfferRepositoryImpl constructor(
	private val offerApi: OfferApi,
	private val myOfferDao: MyOfferDao,
	private val offerDao: OfferDao,
	private val requestedOfferDao: RequestedOfferDao,
	private val locationDao: LocationDao,
	private val contactDao: ContactDao,
	private val offerCommonFriendCrossRefDao: OfferCommonFriendCrossRefDao,
	private val transactionProvider: TransactionProvider
) : OfferRepository {

	override suspend fun createOffer(offerList: List<NewOffer>, expiration: Long): Resource<Offer> = tryOnline(
		request = {
			offerApi.postOffers(
				CreateOfferRequest(
					offerPrivateList = offerList.map { it.toNetwork() },
					expiration = expiration
				)
			)
		},
		mapper = { it?.fromNetwork() }
	)

	override suspend fun updateOffer(offerId: String, offerList: List<NewOffer>): Resource<Offer> = tryOnline(
		request = {
			offerApi.putOffers(
				UpdateOfferRequest(
					offerId = offerId,
					offerPrivateCreateList = offerList.map { it.toNetwork() }
				)
			)
		},
		mapper = { it?.fromNetwork() }
	)

	override suspend fun loadOffersForMe(): Resource<List<Offer>> = tryOnline(
		request = {
			offerApi.getOffersMe()
		},
		mapper = { it?.items?.map { item -> item.fromNetwork() } }
	)

	override suspend fun deleteMyOffers(offerIds: List<String>): Resource<Unit> = tryOnline(
		request = {
			offerApi.deleteOffersId(offerIds = offerIds)
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

	override suspend fun refreshOffer(offerId: String): Resource<List<Offer>> = tryOnline(
		request = { offerApi.getOffersId(listOf(offerId)) },
		mapper = { items -> items?.map { item -> item.fromNetwork() } ?: emptyList() },
		doOnSuccess = {
			it?.let { offers ->
				updateOffers(offers)
			}
		}
	)

	override suspend fun saveMyOfferIdAndKeys(
		offerId: String,
		privateKey: String,
		publicKey: String,
		offerType: String,
		isInboxCreated: Boolean
	): Resource<Unit> {
		myOfferDao.replace(
			MyOfferEntity(
				extId = offerId,
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
			it.offer.fromCache(it.locations, it.commonFriends)
		}

	override suspend fun getOffersFlow() = offerDao.getAllExtendedOffersFlow().map { list ->
		list.map {
			it.offer.fromCache(it.locations, it.commonFriends)
		}
	}

	override suspend fun syncOffers() {
		val newOffers = getNewOffers()

		when (newOffers.status) {
			Status.Success -> {
				overwriteOffers(newOffers.data.orEmpty())
			}
		}
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
				//TODO toCache?
				val commonFriendRefs = contactDao.getContactByHashedPhones(
					// because we don't have full objects at this moment yet (it's from the API, not database)
					offer.commonFriends.map { it.contactHash }
				).map {
					OfferCommonFriendCrossRef(
						offerId = offerId,
						contactId = it.contactId
					)
				}
				offerCommonFriendCrossRefDao.replaceAll(commonFriendRefs)
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
				val commonFriendRefs = contactDao.getContactByHashedPhones(
					// because we don't have full objects at this moment yet (it's from the API, not database)
					offer.commonFriends.map { it.contactHash }
				).map {
					OfferCommonFriendCrossRef(
						offerId = offerId,
						contactId = it.contactId
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
		mapper = { it?.items?.map { item -> item.fromNetwork() } }
	)

	override suspend fun getMyOffersWithoutInbox(): List<MyOffer> =
		myOfferDao.getMyOffersWithoutInbox()
			.map { it.fromCache() }
}