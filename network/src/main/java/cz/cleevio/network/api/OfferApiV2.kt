package cz.cleevio.network.api

import cz.cleevio.network.response.offer.v2.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface OfferApiV2 {

	@GET("offers")
	suspend fun getOffersId(
		@Query(value = "offerIds") offerIds: List<String>
	): Response<List<OfferUnifiedResponseV2>>

	@PUT("offers")
	suspend fun putOffers(
		@Body updateOfferRequestV2: UpdateOfferRequestV2
	): Response<OfferUnifiedResponseV2>

	@POST("offers")
	suspend fun postOffers(
		@Body createOfferRequestV2: OfferCreateRequestV2
	): Response<OfferUnifiedAdminResponseV2>

	@POST("offers/private-part")
	suspend fun postOffersPrivatePart(
		@Body createOfferPrivatePartRequestV2: CreateOfferPrivatePartRequestV2
	): Response<ResponseBody>

	@GET("offers/me")
	suspend fun getOffersMe(): Response<OffersUnifiedResponseV2>

	@GET("offers/me/modified/")
	suspend fun getModifiedOffers(
		@Query("page") page: Int,
		@Query("limit") limit: Int,
		@Query("modifiedAt") modifiedAt: String
	): Response<OffersUnifiedResponseV2>
}