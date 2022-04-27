package cz.cleevio.repository.repository.offer

import cz.cleevio.cache.dao.MyOfferDao
import cz.cleevio.cache.entity.MyOfferEntity
import cz.cleevio.cache.dao.OfferDao
import cz.cleevio.network.api.OfferApi
import cz.cleevio.network.data.Resource
import cz.cleevio.network.data.Status
import cz.cleevio.network.extensions.tryOnline
import cz.cleevio.network.request.offer.CreateOfferRequest
import cz.cleevio.repository.model.offer.*

class OfferRepositoryImpl constructor(
	private val offerApi: OfferApi,
	private val myOfferDao: MyOfferDao,
	private val offerDao: OfferDao
) : OfferRepository {

	override suspend fun createOffer(offerList: List<NewOffer>): Resource<Offer> = tryOnline(
		request = {
			offerApi.postOffers(
				CreateOfferRequest(
					offerPrivateList = offerList.map { it.toNetwork() }
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

	override suspend fun loadOffersCreatedByMe(offerIds: List<Long>): Resource<List<Offer>> = tryOnline(
		request = {
			//todo: change to sending list of strings, as soon as BE changes API
			offerApi.getOffersId(offerId = offerIds.first().toString())
		},
		mapper = { item -> item?.let { listOf(it.fromNetwork()) } ?: emptyList() }
	)

	override suspend fun deleteMyOffers(offerIds: List<Long>): Resource<Unit> = tryOnline(
		request = {
			//todo: change to sending list of strings, as soon as BE changes API
			offerApi.deleteOffersId(offerId = offerIds.first().toString())
		},
		mapper = { }
	)

	override suspend fun saveMyOfferIdAndKeys(offerId: String, privateKey: String, publicKey: String): Resource<Unit> {
		myOfferDao.replace(
			MyOfferEntity(
				extId = offerId,
				privateKey = privateKey,
				publicKey = publicKey
			)
		)
		return Resource.success(data = Unit)
	}

	override suspend fun getOffers() =
		offerDao.getAllOffers().map {
			it.fromCache()
		}

	override suspend fun syncOffers() {
		val newOffers = getNewOffers()

		when (newOffers.status) {
			Status.Success -> {
				offerDao.replaceAll(newOffers.data.orEmpty().map {
					it.toCache()
				})
			}
		}
	}

	private suspend fun getNewOffers(): Resource<List<Offer>> = tryOnline(
		request = {
			offerApi.getModifiedOffers(0, Int.MAX_VALUE, "1970-01-01T00:00:00.000Z")
		},
		mapper = { it?.items?.map { item -> item.fromNetwork() } }
	)
}