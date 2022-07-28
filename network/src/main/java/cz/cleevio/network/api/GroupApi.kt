package cz.cleevio.network.api

import cz.cleevio.network.request.group.CreateGroupRequest
import cz.cleevio.network.request.group.JoinGroupRequest
import cz.cleevio.network.request.group.LeaveGroupRequest
import cz.cleevio.network.request.group.NewMemberRequest
import cz.cleevio.network.response.group.GroupCreatedResponse
import cz.cleevio.network.response.group.GroupResponse
import cz.cleevio.network.response.group.GroupsResponse
import cz.cleevio.network.response.group.NewMembersResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface GroupApi {

	//leave group
	@PUT("groups/leave")
	suspend fun putGroupsLeave(
		@Body leaveGroupRequest: LeaveGroupRequest
	): Response<ResponseBody>

	//get groups by code
	@GET("groups")
	suspend fun getGroups(
		@Query(value = "code") code: String
	): Response<GroupResponse>

	//create new group
	@POST("groups")
	suspend fun postGroups(
		@Body createGroupRequest: CreateGroupRequest
	): Response<GroupCreatedResponse>

	//get new members of group
	@POST("groups/members/new")
	suspend fun postGroupsMembersNew(
		@Body newMemberRequest: NewMemberRequest
	): Response<NewMembersResponse>

	//join group via code
	@POST("groups/join")
	suspend fun postGroupsJoin(
		@Body joinGroupRequest: JoinGroupRequest
	): Response<ResponseBody>

	//return mine groups
	@GET("groups/me")
	suspend fun getGroupsMe(): Response<GroupsResponse>

	//return mine expired groups
	@GET("groups/expired")
	suspend fun getGroupsExpired(
		@Query(value = "groupUuids") groupUuids: List<String>
	): Response<GroupsResponse>
}