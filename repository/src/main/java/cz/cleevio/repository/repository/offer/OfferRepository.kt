package cz.cleevio.repository.repository.offer

import cz.cleevio.network.data.Resource

interface OfferRepository {

	suspend fun createOffer(): Resource<Any>
}