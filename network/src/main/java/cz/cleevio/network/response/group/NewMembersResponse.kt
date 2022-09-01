package cz.cleevio.network.response.group

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NewMembersResponse constructor(
	val newMembers: List<GroupMembers>
)

@JsonClass(generateAdapter = true)
data class GroupMembers constructor(
	val groupUuid: String,
	val newPublicKeys: List<String>,
	val removedPublicKeys: List<String>,
)
