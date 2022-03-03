package cz.cleevio.network.api

import cz.cleevio.network.request.contact.ContactRequest
import cz.cleevio.network.request.contact.DeleteContactRequest
import cz.cleevio.network.response.contact.ContactImportResponse
import cz.cleevio.network.response.contact.ContactResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*


interface ContactApi {

	@POST("contact/import")
	suspend fun postContactImport(
		@Body contactImportRequest: ContactRequest
	): Response<ContactImportResponse>

	@GET("contact")
	suspend fun getContact(): Response<ContactResponse>

	@DELETE("contact")
	suspend fun deleteContact(
		@Body deleteContactRequest: DeleteContactRequest
	): Response<ResponseBody>    //204 expected

	//TODO: ask why not POST and response should be same as ContactImportResponse
	@GET("contact/new")
	suspend fun getContactNew(
		@Query(value = "contactsRequest") contactRequest: ContactRequest
	): Response<ContactImportResponse>


}