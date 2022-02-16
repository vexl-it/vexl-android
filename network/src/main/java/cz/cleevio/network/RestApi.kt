package cz.cleevio.network

interface RestApi {

	companion object {
		const val HEADER_AUTHORIZATION = "Authorization"
		private const val HEADER_AUTHORIZATION_BEARER = "Bearer %s"

		fun createTokenHeader(token: String?): String =
			String.format(HEADER_AUTHORIZATION_BEARER, token.orEmpty())
	}

	// TODO rest api WIP
	/* * * * Auth * * * *//*

	@POST("auth/login")
	suspend fun login(@Header(HEADER_AUTHORIZATION) credentials: String): Response<TokenResponse>

	@POST("auth/logout")
	suspend fun logout(@Header(HEADER_AUTHORIZATION) token: String): Response<Any>

	@POST("auth/login")
	suspend fun login(@Header(HEADER_AUTHORIZATION) credentials: String): Response<TokenResponse>

	@POST("auth/logout")
	suspend fun logout(@Header(HEADER_AUTHORIZATION) token: String): Response<Any>


	*//* * * * User * * * *//*

	@POST("user")
	suspend fun createUser(@Body createUserRequest: CreateUserRequest): Response<TokenResponse>

	@GET("user/me")
	suspend fun userInfo(@Header(HEADER_AUTHORIZATION) token: String): Response<UserInfoResponse>

	@GET("user/confirm")
	suspend fun userStatus(@Header(HEADER_AUTHORIZATION) token: String): Response<UserValidationResponse>

	@PUT("user/pin")
	suspend fun changePin(
		@Header(HEADER_AUTHORIZATION) token: String,
		@Body changePinRequest: ChangePinRequest
	): Response<List<TokenResponse>>

	@GET("user/pin/recovery")
	suspend fun resetPin(@Header(HEADER_AUTHORIZATION) token: String): Response<Any>

	@POST("user/confirm/email")
	suspend fun resendEmailConfirmation(@Header(HEADER_AUTHORIZATION) token: String): Response<UserValidationResponse>

	@PUT("user/confirm/email")
	suspend fun confirmEmail(
		@Header(HEADER_AUTHORIZATION) token: String,
		@Body confirmationRequest: ConfirmationRequest
	): Response<UserValidationResponse>

	@POST("user/confirm/phone")
	suspend fun addPhoneNumber(
		@Header(HEADER_AUTHORIZATION) token: String,
		@Body phoneNumber: PhoneNumber
	): Response<UserValidationResponse>

	@POST("user/confirm/phone")
	suspend fun resendPhoneNumberSmsCode(
		@Header(HEADER_AUTHORIZATION) token: String,
		@Body phoneNumber: PhoneNumber
	): Response<UserValidationResponse>

	@PUT("user/confirm/phone")
	suspend fun confirmPhoneNumber(
		@Header(HEADER_AUTHORIZATION) token: String,
		@Body confirmationRequest: ConfirmationRequest
	): Response<UserValidationResponse>

	@POST("user/confirm/kyc")
	suspend fun confirmKycPendingStatus(
		@Header(HEADER_AUTHORIZATION) token: String,
		@Body kycPendingStatusRequest: KycPendingStatusRequest
	): Response<UserValidationResponse>

	@PUT("user/{id}")
	suspend fun updateUserCredetials(
		@Header(HEADER_AUTHORIZATION) token: String,
		@Path(value = "id") userId: String,
		@Body updateUserCredetialsRequest: UpdateUserCredetialsRequest
	): Response<UserInfoResponse>

	@PUT("user/{id}")
	suspend fun updateUserCredetialsWithAvatar(
		@Header(HEADER_AUTHORIZATION) token: String,
		@Path(value = "id") userId: String,
		@Body updateUserCredetialsWithAvatarRequest: UpdateUserCredetialsWithAvatarRequest
	): Response<UserInfoResponse>

	@PUT("user/{id}")
	suspend fun updateEmail(
		@Header(HEADER_AUTHORIZATION) token: String,
		@Path(value = "id") userId: String,
		@Body updateEmailRequest: UpdateEmailRequest
	): Response<UserInfoResponse>

	@PUT("user/{id}")
	suspend fun updatePublicSettings(
		@Header(HEADER_AUTHORIZATION) token: String,
		@Path(value = "id") userId: String,
		@Body publicSettingsRequest: UpdatePublicSettingsRequest
	): Response<UserInfoResponse>

	@PUT("user/{id}")
	suspend fun updateNotificationSettings(
		@Header(HEADER_AUTHORIZATION) token: String,
		@Path(value = "id") userId: String,
		@Body notificationSettingsRequest: UpdateNotificationSettingsRequest
	): Response<UserInfoResponse>

	@POST("user/password/recovery")
	suspend fun resetPassword(@Body resetPasswordRequest: ResetPasswordRequest): Response<Any>

	@PUT("user/password/recovery")
	suspend fun setNewPassword(@Body setNewPasswordRequest: SetNewPasswordRequest): Response<List<TokenResponse>>

	@PUT("user/password")
	suspend fun changePassword(
		@Header(HEADER_AUTHORIZATION) token: String,
		@Body changePasswordRequest: ChangePasswordRequest
	): Response<TokenResponse>

	@GET("user")
	suspend fun getUsers(
		@Header(HEADER_AUTHORIZATION) token: String,
		@Query("state") state: String?,
		@Query("order") order: String?,
		@Query("direction") direction: String?,
		@Query("search") search: String?,
		@Query("confirmed") confirmed: Boolean?
	): Response<BasePagedResponse<User>>

	@GET
	suspend fun getNextUsers(
		@Url url: String,
		@Header(HEADER_AUTHORIZATION) token: String
	): Response<BasePagedResponse<User>>

	@POST("push")
	suspend fun sendFirebaseToken(
		@Header(HEADER_AUTHORIZATION) token: String,
		@Body firebaseTokenRequest: FirebaseTokenRequest
	): Response<BaseResponse>
*/
}