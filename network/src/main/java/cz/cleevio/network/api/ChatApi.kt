package cz.cleevio.network.api

import cz.cleevio.network.request.chat.*
import cz.cleevio.network.response.chat.ChallengeCreatedResponse
import cz.cleevio.network.response.chat.InboxResponse
import cz.cleevio.network.response.chat.MessageResponse
import cz.cleevio.network.response.chat.MessagesResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT

interface ChatApi {

	//update inbox with new firebase token
	@PUT("inboxes")
	suspend fun putInboxes(
		@Body inboxRequest: UpdateInboxRequest
	): Response<InboxResponse>

	//create new inbox
	@POST("inboxes")
	suspend fun postInboxes(
		@Body inboxRequest: CreateInboxRequest
	): Response<ResponseBody>

	//retrieve messages from inbox
	@PUT("inboxes/messages")
	suspend fun putInboxesMessages(
		@Body messageRequest: MessageRequest
	): Response<MessagesResponse>

	//send a message to inbox
	@POST("inboxes/messages")
	suspend fun postInboxesMessages(
		@Body sendMessageRequest: SendMessageRequest
	): Response<MessageResponse>

	//block/unblock sender
	@PUT("inboxes/block")
	suspend fun putInboxesBlock(
		@Body blockRequest: BlockInboxRequest
	): Response<ResponseBody>

	//approve communication request
	@POST("inboxes/approval/request")
	suspend fun postInboxesApprovalRequest(
		@Body approvalRequest: ApprovalRequest
	): Response<MessageResponse>

	//approve communication request
	@POST("inboxes/approval/confirm")
	suspend fun postInboxesApprovalConfirm(
		@Body approvalRequest: ApprovalConfirmRequest
	): Response<MessageResponse>

	//delete whole inbox
	@HTTP(method = "DELETE", path = "inboxes", hasBody = true)
	suspend fun deleteInboxes(
		@Body deletionRequest: DeletionRequest
	): Response<ResponseBody>

	//delete pulled messages
	@HTTP(method = "DELETE", path = "inboxes/messages", hasBody = true)
	suspend fun deleteInboxesMessages(
		@Body deletionRequest: DeletionRequest
	): Response<ResponseBody>

	//create a new challenge
	@POST("challenges")
	suspend fun postChallenge(
		@Body challengeRequest: CreateChallengeRequest
	): Response<ChallengeCreatedResponse>
}