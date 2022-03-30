package cz.cleevio.network.api

import cz.cleevio.network.request.contact.ContactRequest
import cz.cleevio.network.request.contact.ContactUserRequest
import cz.cleevio.network.request.contact.DeleteContactRequest
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
		@Body contactImportRequest: ContactRequest
	): Response<ContactImportResponse>

	@GET("contacts/me")
	suspend fun getContactsMe(): Response<BasePagedResponse<ContactResponse>>

	@DELETE("contact")
	suspend fun deleteContact(
		@Body deleteContactRequest: DeleteContactRequest
	): Response<ResponseBody>

	@POST("users")
	suspend fun postUsers(
		@Body contactUserRequest: ContactUserRequest
	): Response<ContactUserResponse>

	@DELETE("users/me")
	suspend fun deleteUserMe(): Response<ResponseBody>

	@GET("facebook/{facebookId}/token/{accessToken}")
	suspend fun getFacebookUser(
		@Path(value = "facebookId") facebookId: String,
		@Path(value = "accessToken") accessToken: String
	): Response<ContactFacebookResponse>

	@GET("facebook/{facebookId}/token/{accessToken}/new")
	suspend fun getFacebookTokenNew(
		@Path(value = "facebookId") facebookId: String,
		@Path(value = "accessToken") accessToken: String
	): Response<ContactFacebookResponse>
}