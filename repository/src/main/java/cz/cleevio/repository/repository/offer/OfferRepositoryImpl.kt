package cz.cleevio.repository.repository.offer

import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.network.api.OfferApi
import cz.cleevio.network.data.Resource

class OfferRepositoryImpl constructor(
	private val offerApi: OfferApi,
	private val encryptedPreference: EncryptedPreferenceRepository
) : OfferRepository {

	override suspend fun createOffer(): Resource<Any> {
		TODO("Not yet implemented")
	}
}