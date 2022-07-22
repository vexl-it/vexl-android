package cz.cleevio.network.request.group

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NewMemberRequest constructor(
	val groups: List<GroupRequest>
)

@JsonClass(generateAdapter = true)
data class GroupRequest constructor(
	val groupUuid: String,
	val publicKeys: List<String>
)
