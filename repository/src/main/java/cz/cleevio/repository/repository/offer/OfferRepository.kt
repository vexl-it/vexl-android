package cz.cleevio.repository.repository.offer

import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.network.data.Resource
import cz.cleevio.network.request.offer.DeletePrivatePartRequest
import cz.cleevio.repository.model.offer.LocationSuggestion
import cz.cleevio.repository.model.offer.MyOffer
import cz.cleevio.repository.model.offer.NewOffer
import cz.cleevio.repository.model.offer.Offer
import kotlinx.coroutines.flow.Flow

@Suppress("TooManyFunctions")
interface OfferRepository {

	//you have to supply list of encrypted offers. 1 for each of your contacts, encrypted with contact's key
	suspend fun createOffer(offerList: List<NewOffer>, expiration: Long, offerKeys: KeyPair): Resource<Offer>

	//you have to supply list of encrypted offers. 1 for each of your contacts, encrypted with contact's key
	suspend fun updateOffer(offerId: String, offerList: List<NewOffer>): Resource<Offer>

	//todo: will probably have to use Paging Adapter and return paging data somewhere
	suspend fun loadOffersForMe(): Resource<List<Offer>>

	suspend fun deleteMyOffers(offerIds: List<String>): Resource<Unit>

	suspend fun deleteOfferById(offerId: String): Resource<Unit>

	suspend fun deleteOfferForPublicKeys(deletePrivatePartRequest: DeletePrivatePartRequest): Resource<Unit>

	//not needed right now?
	suspend fun refreshOffer(offerId: String): Resource<List<Offer>>

	suspend fun saveMyOfferIdAndKeys(
		offerId: String,
		privateKey: String,
		publicKey: String,
		offerType: String,
		isInboxCreated: Boolean
	): Resource<Unit>

	suspend fun loadOfferKeysByOfferId(offerId: String): KeyPair?

	suspend fun getOffers(): List<Offer>

	suspend fun getOffersFlow(): Flow<List<Offer>>

	suspend fun syncOffers()

	suspend fun getMyOffersCount(offerType: String): Int

	suspend fun getMyOffersWithoutInbox(): List<MyOffer>

	suspend fun getLocationSuggestions(count: Int, query: String, language: String): Resource<List<LocationSuggestion>>
}
