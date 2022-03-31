package cz.cleevio.network.api

import cz.cleevio.network.request.offer.CreateOfferRequest
import cz.cleevio.network.request.offer.UpdateOfferRequest
import cz.cleevio.network.response.BasePagedResponse
import cz.cleevio.network.response.offer.OfferPublicResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*


interface OfferApi {

	@PUT("offers")
	suspend fun putOffers(
		@Body updateOfferRequest: UpdateOfferRequest
	): Response<OfferPublicResponse>

	@POST("offers")
	suspend fun postOffers(
		@Body createOfferRequest: CreateOfferRequest
		//): Response<OfferPublicResponse>	//todo: ask for OfferPublicResponse instead of No content
	): Response<ResponseBody>

	@GET("offers/{offerId}")
	suspend fun getOffersId(
		@Path(value = "offerId") offerId: String,
	): Response<OfferPublicResponse>

	@DELETE("offers/{offerId}")
	suspend fun deleteOffersId(
		@Path(value = "offerId") offerId: String,
	): Response<ResponseBody>

	@GET("offers/me")
	suspend fun getOffersMe(): Response<BasePagedResponse<OfferPublicResponse>>    //todo: ask for OfferPublicResponse
}