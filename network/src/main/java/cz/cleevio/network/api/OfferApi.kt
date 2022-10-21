package cz.cleevio.network.api

import cz.cleevio.network.request.offer.DeletePrivatePartRequest
import cz.cleevio.network.request.offer.ReportOfferRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface OfferApi {

	@DELETE("offers")
	suspend fun deleteOffersId(
		@Query(value = "adminIds") adminIds: List<String>
	): Response<ResponseBody>

	@HTTP(method = "DELETE", path = "offers/private-part", hasBody = true)
	suspend fun deleteOffersPrivatePart(
		@Body deletePrivatePartRequest: DeletePrivatePartRequest
	): Response<ResponseBody>

	@POST("offers/report")
	suspend fun postOffersReport(
		@Body reportOfferRequest: ReportOfferRequest
	): Response<ResponseBody>
}
