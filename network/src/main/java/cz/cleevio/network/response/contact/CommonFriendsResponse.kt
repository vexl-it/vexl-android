package cz.cleevio.network.response.contact

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommonFriendsResponse constructor(
	val commonContacts: List<CommonFriendResponse>
)

@JsonClass(generateAdapter = true)
data class CommonFriendResponse(
	val publicKey: String,
	val common: Common
)

@JsonClass(generateAdapter = true)
data class Common(
	val hashes: List<String>
)
