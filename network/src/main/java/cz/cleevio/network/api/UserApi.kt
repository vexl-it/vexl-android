package cz.cleevio.network.api

import cz.cleevio.network.request.user.ConfirmChallengeRequest
import cz.cleevio.network.request.user.ConfirmCodeRequest
import cz.cleevio.network.request.user.ConfirmPhoneRequest
import cz.cleevio.network.request.user.UserRequest
import cz.cleevio.network.response.user.ConfirmCodeResponse
import cz.cleevio.network.response.user.ConfirmPhoneResponse
import cz.cleevio.network.response.user.SignatureResponse
import cz.cleevio.network.response.user.UserResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

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

	@GET("user/me")
	suspend fun getUserMe(): Response<UserResponse>

	@POST("user")
	suspend fun postUser(
		@Body userRequest: UserRequest
	): Response<UserResponse>

	@PUT("user/me")
	suspend fun putUserMe(
		@Body userRequest: UserRequest
	): Response<UserResponse>

	@DELETE("user/me/avatar")
	suspend fun deleteAvatar(): Response<ResponseBody>

	@DELETE("user/me")
	suspend fun deleteUserMe(): Response<ResponseBody>

	@GET("user/signature/{facebookId}")
	suspend fun getUserSignatureFacebook(
		@Path(value = "facebookId") facebookId: String
	): Response<SignatureResponse>
}
