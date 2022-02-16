package cz.cleevio.network

import cz.cleevio.network.response.TokenResponse
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.POST

interface TokenRestApi {

	@POST("auth/refresh")
	fun refreshToken(@Header(RestApi.HEADER_AUTHORIZATION) token: String): Call<TokenResponse>
}