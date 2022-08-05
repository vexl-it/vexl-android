package cz.cleevio.network.api

import cz.cleevio.network.request.offer.CreateOfferRequest
import cz.cleevio.network.request.offer.DeletePrivatePartRequest
import cz.cleevio.network.request.offer.UpdateOfferRequest
import cz.cleevio.network.response.BasePagedResponse
import cz.cleevio.network.response.offer.LocationSuggestionResponse
import cz.cleevio.network.response.offer.OfferUnifiedResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface OfferApi {

	@PUT("offers")
	suspend fun putOffers(
		@Body updateOfferRequest: UpdateOfferRequest
	): Response<OfferUnifiedResponse>

	@POST("offers")
	suspend fun postOffers(
		@Body createOfferRequest: CreateOfferRequest
	): Response<OfferUnifiedResponse>

	@GET("offers")
	suspend fun getOffersId(
		@Query(value = "offerIds") offerIds: List<String>
	): Response<List<OfferUnifiedResponse>>

	@DELETE("offers")
	suspend fun deleteOffersId(
		@Query(value = "offerIds") offerIds: List<String>
	): Response<ResponseBody>

	@GET("offers/me")
	suspend fun getOffersMe(): Response<BasePagedResponse<OfferUnifiedResponse>>

	@GET("offers/me/modified/")
	suspend fun getModifiedOffers(
		@Query("page") page: Int,
		@Query("limit") limit: Int,
		@Query("modifiedAt") modifiedAt: String
	): Response<BasePagedResponse<OfferUnifiedResponse>>

	@HTTP(method = "DELETE", path = "offers/private-part", hasBody = true)
	suspend fun deleteOffersPrivatePart(
		@Body deletePrivatePartRequest: DeletePrivatePartRequest
	): Response<ResponseBody>

	@GET("suggest")
	suspend fun getSuggestions(
		@Query("count") count: Int,
		@Query("phrase") phrase: String,
		@Query("lang") languages: String
	): Response<LocationSuggestionResponse>
}
