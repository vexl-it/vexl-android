package cz.cleevio.network.request.group

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LeaveGroupRequest constructor(
	//should be SHA-256
	val groupUuid: String
)
