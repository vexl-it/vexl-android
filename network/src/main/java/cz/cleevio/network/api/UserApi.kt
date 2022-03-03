package cz.cleevio.network.api

import cz.cleevio.network.request.user.*
import cz.cleevio.network.response.user.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface UserApi {

	@POST("user/confirm/phone")
	suspend fun postUserConfirmPhone(
		@Body confirmPhoneRequest: ConfirmPhoneRequest
	): Response<ConfirmPhoneResponse>

	@POST("user/confirm/code")
	suspend fun postUserConfirmCode(
		@Body confirmPhoneRequest: ConfirmCodeRequest
	): Response<ConfirmCodeResponse>

	@POST("user/confirm/challenge")
	suspend fun postUserConfirmChallenge(
		@Body confirmChallengeRequest: ConfirmChallengeRequest
	): Response<SignatureResponse>

	@POST("user/username/available")
	suspend fun postUserUsernameAvailable(
		@Body usernameAvailableRequest: UsernameAvailableRequest
	): Response<UsernameAvailableResponse>

	@GET("user")
	suspend fun getUser(): Response<UserResponse>

	@POST("user")
	suspend fun postUser(
		@Body userRequest: UserRequest
	): Response<UserResponse>

	@PUT("user")
	suspend fun putUser(
		@Body userRequest: UserRequest
	): Response<UserResponse>

	@DELETE("user")
	suspend fun deleteUser(): Response<ResponseBody>

	@GET("user/signature/{facebookId}")
	suspend fun getUserSignatureFacebook(
		@Path(value = "facebookId") facebookId: String
	): Response<SignatureResponse>
}