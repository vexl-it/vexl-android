package cz.cleevio.repository.repository.offer

import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.offer.NewOffer
import cz.cleevio.repository.model.offer.Offer

interface OfferRepository {

	//you have to supply list of encrypted offers. 1 for each of your contacts, encrypted with contact's key
	suspend fun createOffer(location: String, offerList: List<NewOffer>): Resource<Offer>

	//todo: will probably have to use Paging Adapter and return paging data somewhere
	suspend fun loadOffersForMe(): Resource<List<Offer>>

	suspend fun loadOffersCreatedByMe(offerIds: List<Long>): Resource<List<Offer>>
}