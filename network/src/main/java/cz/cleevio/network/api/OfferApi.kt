package cz.cleevio.network.api

import cz.cleevio.network.request.offer.CreateOfferRequest
import cz.cleevio.network.request.offer.UpdateOfferRequest
import cz.cleevio.network.response.BasePagedResponse
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

	@GET("offers/{offerId}")
	suspend fun getOffersId(
		//todo: BE will probably change this EP to support multiple IDs in single request
		@Path(value = "offerId") offerId: String
	): Response<OfferUnifiedResponse>

	@DELETE("offers/{offerId}")
	suspend fun deleteOffersId(
		@Path(value = "offerId") offerId: String
	): Response<ResponseBody>

	@GET("offers/me")
	//todo: BE will probably support filters on this EP
	suspend fun getOffersMe(): Response<BasePagedResponse<OfferUnifiedResponse>>
}