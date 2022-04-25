package cz.cleevio.repository.repository.offer

import cz.cleevio.network.api.OfferApi
import cz.cleevio.network.data.Resource
import cz.cleevio.network.extensions.tryOnline
import cz.cleevio.network.request.offer.CreateOfferRequest
import cz.cleevio.repository.model.offer.NewOffer
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.model.offer.fromNetwork
import cz.cleevio.repository.model.offer.toNetwork

class OfferRepositoryImpl constructor(
	private val offerApi: OfferApi
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
}