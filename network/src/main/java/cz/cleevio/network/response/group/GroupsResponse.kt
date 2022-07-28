package cz.cleevio.network.response.group

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GroupsResponse constructor(
	val groupResponse: List<GroupResponse>
)

@JsonClass(generateAdapter = true)
data class GroupResponse constructor(
	val uuid: String,
	val name: String,
	val logoUrl: String,
	val createdAt: Long,
	val expirationAt: Long,
	val closureAt: Long,
	val code: Long,
	val memberCount: Long
)
