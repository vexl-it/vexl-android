package cz.cleevio.repository.repository.offer

import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.network.data.Resource
import cz.cleevio.network.request.offer.DeletePrivatePartRequest
import cz.cleevio.repository.model.offer.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Suppress("TooManyFunctions")
interface OfferRepository {

	val buyOfferFilter: MutableStateFlow<OfferFilter>

	val sellOfferFilter: MutableStateFlow<OfferFilter>

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

	fun getOffersFlow(): Flow<List<Offer>>

	fun getFilteredAndSortedOffersByTypeFlow(
		offerTypeName: String,
		offerFilter: OfferFilter
	): Flow<List<Offer>>

	fun getOffersSortedByDateOfCreationFlow(offerTypeName: String): Flow<List<Offer>>

	suspend fun syncOffers()

	suspend fun getMyOffersCount(offerType: String): Int

	suspend fun getMyOffersWithoutInbox(): List<MyOffer>

	suspend fun getLocationSuggestions(count: Int, query: String, language: String): Resource<List<LocationSuggestion>>

	suspend fun clearOfferTables()
}
