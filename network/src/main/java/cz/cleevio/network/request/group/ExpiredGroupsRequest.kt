package cz.cleevio.network.request.group

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExpiredGroupsRequest constructor(
	val uuids: List<String>
)
