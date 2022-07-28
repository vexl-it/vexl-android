package cz.cleevio.network.api

import cz.cleevio.network.interceptors.AuthInterceptor
import cz.cleevio.network.request.contact.ContactRequest
import cz.cleevio.network.request.contact.CreateUserRequest
import cz.cleevio.network.request.contact.DeleteContactRequest
import cz.cleevio.network.request.contact.FirebaseTokenUpdateRequest
import cz.cleevio.network.response.BasePagedResponse
import cz.cleevio.network.response.contact.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ContactApi {

	@POST("contacts/not-imported")
	suspend fun postContactNotImported(
		@Body contactRequest: ContactRequest
	): Response<ContactNotImportResponse>

	@POST("contacts/import")
	suspend fun postContactImport(
		@Header(AuthInterceptor.HEADER_HASH) hash: String? = null,
		@Header(AuthInterceptor.HEADER_SIGNATURE) signature: String? = null,
		@Body contactImportRequest: ContactRequest
	): Response<ContactImportResponse>

	@GET("contacts/me")
	suspend fun getContactsMe(
		@Query("page") page: Int,
		@Query("limit") limit: Int,
		@Query("level") levelApi: ContactLevelApi
	): Response<BasePagedResponse<ContactResponse>>

	//todo: should this be ever used?
	@DELETE("contact")
	suspend fun deleteContact(
		@Header(AuthInterceptor.HEADER_HASH) hash: String? = null,
		@Header(AuthInterceptor.HEADER_SIGNATURE) signature: String? = null,
		@Body deleteContactRequest: DeleteContactRequest
	): Response<ResponseBody>

	@POST("users")
	suspend fun postUsers(
		@Header(AuthInterceptor.HEADER_HASH) hash: String? = null,
		@Header(AuthInterceptor.HEADER_SIGNATURE) signature: String? = null,
		@Body createUserRequest: CreateUserRequest
	): Response<Unit>

	@PUT("users")
	suspend fun putUsers(
		@Header(AuthInterceptor.HEADER_HASH) hash: String? = null,
		@Header(AuthInterceptor.HEADER_SIGNATURE) signature: String? = null,
		@Body firebaseTokenUpdateRequest: FirebaseTokenUpdateRequest
	): Response<Unit>

	@DELETE("users/me")
	suspend fun deleteUserMe(
		@Header(AuthInterceptor.HEADER_HASH) hash: String? = null,
		@Header(AuthInterceptor.HEADER_SIGNATURE) signature: String? = null,
	): Response<ResponseBody>

	@GET("facebook/{facebookId}/token/{accessToken}")
	suspend fun getFacebookUser(
		@Header(AuthInterceptor.HEADER_HASH) hash: String? = null,
		@Header(AuthInterceptor.HEADER_SIGNATURE) signature: String? = null,
		@Path(value = "facebookId") facebookId: String,
		@Path(value = "accessToken") accessToken: String
	): Response<ContactFacebookResponse>

	//todo: delete?
	@GET("facebook/{facebookId}/token/{accessToken}/new")
	suspend fun getFacebookTokenNew(
		@Header(AuthInterceptor.HEADER_HASH) hash: String? = null,
		@Header(AuthInterceptor.HEADER_SIGNATURE) signature: String? = null,
		@Path(value = "facebookId") facebookId: String,
		@Path(value = "accessToken") accessToken: String
	): Response<ContactFacebookResponse>

	@POST("../../temp")
	suspend fun generateContactsTmp(
		@Header(AuthInterceptor.HEADER_HASH) hash: String? = null,
		@Header(AuthInterceptor.HEADER_SIGNATURE) signature: String? = null,
	): Response<ResponseBody>

	@GET("contacts/common")
	suspend fun getCommonContacts(
		@Header(AuthInterceptor.HEADER_HASH) hash: String? = null,
		@Header(AuthInterceptor.HEADER_SIGNATURE) signature: String? = null,
		@Query(value = "publicKeys") publicKeys: Collection<String>
	): Response<CommonFriendsResponse>
}