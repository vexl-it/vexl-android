package cz.cleevio.network

import cz.cleevio.network.response.offer.LocationSuggestionResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LocationApi {

	@GET("suggest")
	suspend fun getSuggestions(
		@Query("count") count: Int,
		@Query("phrase") phrase: String,
		@Query("lang") languages: String
	): Response<LocationSuggestionResponse>
}
