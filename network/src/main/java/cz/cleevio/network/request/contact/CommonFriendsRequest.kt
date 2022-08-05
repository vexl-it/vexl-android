package cz.cleevio.network.request.contact

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommonFriendsRequest constructor(
	val publicKeys: Collection<String>
)
