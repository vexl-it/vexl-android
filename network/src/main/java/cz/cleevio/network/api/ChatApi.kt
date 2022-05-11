package cz.cleevio.network.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApi {

	//TODO: this is just example
	@POST("chat/message")
	suspend fun postMessage(
		@Body messageRequest: Any
	): Response<Any>
}