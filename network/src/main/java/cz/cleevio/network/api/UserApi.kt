package cz.cleevio.network.api

import cz.cleevio.network.request.user.ConfirmChallengeRequest
import cz.cleevio.network.request.user.ConfirmCodeRequest
import cz.cleevio.network.request.user.ConfirmPhoneRequest
import cz.cleevio.network.response.user.ConfirmCodeResponse
import cz.cleevio.network.response.user.ConfirmPhoneResponse
import cz.cleevio.network.response.user.SignatureResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApi {

	@POST("user/confirmation/phone")
	suspend fun postUserConfirmPhone(
		@Body confirmPhoneRequest: ConfirmPhoneRequest
	): Response<ConfirmPhoneResponse>

	@POST("user/confirmation/code")
	suspend fun postUserConfirmCode(
		@Body confirmPhoneRequest: ConfirmCodeRequest
	): Response<ConfirmCodeResponse>

	@POST("user/confirmation/challenge")
	suspend fun postUserConfirmChallenge(
		@Body confirmChallengeRequest: ConfirmChallengeRequest
	): Response<SignatureResponse>

	@GET("user/signature/{facebookId}")
	suspend fun getUserSignatureFacebook(
		@Path(value = "facebookId") facebookId: String
	): Response<SignatureResponse>
}
