package cz.cleevio.repository.repository.offer

import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.offer.NewOffer
import cz.cleevio.repository.model.offer.Offer
import kotlinx.coroutines.flow.Flow

interface OfferRepository {

	//you have to supply list of encrypted offers. 1 for each of your contacts, encrypted with contact's key
	suspend fun createOffer(offerList: List<NewOffer>, expiration: Long): Resource<Offer>

	//you have to supply list of encrypted offers. 1 for each of your contacts, encrypted with contact's key
	suspend fun updateOffer(offerId: String, offerList: List<NewOffer>): Resource<Offer>

	//todo: will probably have to use Paging Adapter and return paging data somewhere
	suspend fun loadOffersForMe(): Resource<List<Offer>>

	suspend fun loadOffersCreatedByMe(offerIds: List<Long>): Resource<List<Offer>>

	suspend fun deleteMyOffers(offerIds: List<Long>): Resource<Unit>

	suspend fun saveMyOfferIdAndKeys(
		offerId: String,
		privateKey: String,
		publicKey: String,
		offerType: String
	): Resource<Unit>

	suspend fun loadOfferKeysByOfferId(offerId: String): KeyPair?

	suspend fun getOffers(): List<Offer>

	suspend fun getOffersFlow(): Flow<List<Offer>>

	suspend fun syncOffers()

	suspend fun getMyOffersCount(offerType: String): Int
}