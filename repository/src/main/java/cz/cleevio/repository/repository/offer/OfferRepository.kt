package cz.cleevio.repository.repository.offer

import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.network.data.Resource
import cz.cleevio.network.request.offer.DeletePrivatePartRequest
import cz.cleevio.repository.model.offer.LocationSuggestion
import cz.cleevio.repository.model.offer.MyOffer
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.model.offer.OfferFilter
import cz.cleevio.repository.model.offer.v2.NewOfferPrivateV2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Suppress("TooManyFunctions")
interface OfferRepository {

	val buyOfferFilter: MutableStateFlow<OfferFilter>

	val sellOfferFilter: MutableStateFlow<OfferFilter>

	val isOfferSyncInProgress: MutableStateFlow<Boolean>

	//you have to supply list of encrypted offers. 1 for each of your contacts, encrypted with contact's key
	@Suppress("LongParameterList")
	suspend fun createOffer(
		offerList: List<NewOfferPrivateV2?>,
		expiration: Long,
		offerKeys: KeyPair,
		offerType: String,
		encryptedFor: List<String>,
		payloadPublic: String,
		symmetricalKey: String,
		friendLevel: String,
	): Resource<Offer>

	suspend fun createOfferForPublicKeys(
		offerId: String,
		offerList: List<NewOfferPrivateV2?>,
		additionalEncryptedFor: List<String>
	): Resource<Unit>

	//you have to supply list of encrypted offers. 1 for each of your contacts, encrypted with contact's key
	suspend fun updateOffer(
		offerId: String,
		offerList: List<NewOfferPrivateV2?>,
		additionalEncryptedFor: List<String>,
		payloadPublic: String,
	): Resource<Offer>

	suspend fun loadOffersForMe(): Resource<List<Offer>>

	suspend fun deleteMyOffers(offerIds: List<String>): Resource<Unit>

	suspend fun deleteMyOfferById(offerId: String): Resource<Unit>

	suspend fun deleteBrokenMyOffersFromDB(offerIds: List<String>)

	suspend fun deleteOfferForPublicKeys(
		deletePrivatePartRequest: DeletePrivatePartRequest
	): Resource<Unit>

	@Suppress("LongParameterList")
	suspend fun saveMyOfferIdAndKeys(
		offerId: String,
		adminId: String,
		privateKey: String,
		publicKey: String,
		offerType: String,
		isInboxCreated: Boolean,
		encryptedFor: List<String>,
		symmetricalKey: String,
		friendLevel: String
	): Resource<Unit>

	suspend fun loadOfferKeysByOfferId(offerId: String): KeyPair?

	suspend fun loadSymmetricalKeyByOfferId(offerId: String): String?

	suspend fun getOffers(): List<Offer>

	fun getOffersFlow(): Flow<List<Offer>>

	fun getFilteredAndSortedOffersByTypeFlow(
		offerTypeName: String,
		offerFilter: OfferFilter
	): Flow<List<Offer>>

	fun getOffersSortedByDateOfCreationFlow(offerTypeName: String): Flow<List<Offer>>

	suspend fun syncOffers()

	suspend fun getMyActiveOffersCount(offerType: String): Int

	suspend fun getMyOffersWithoutInbox(): List<MyOffer>

	suspend fun getMyOffers(version: Long? = null): List<MyOffer>

	suspend fun getLocationSuggestions(count: Int, query: String, language: String): Resource<List<LocationSuggestion>>

	suspend fun clearOfferTables()

	suspend fun getOfferById(offerId: String): Offer?

	suspend fun reportOffer(offerId: String): Resource<Unit>

	//not used right now, could be helpful later
	suspend fun forceReEncrypt()

	suspend fun refreshOffers(): Resource<Unit>
}
