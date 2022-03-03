package cz.cleevio.network.response.contact

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContactFacebookResponse constructor(
	val facebookUser: FacebookUserResponse
)

@JsonClass(generateAdapter = true)
data class FacebookUserResponse constructor(
	val id: String,
	val name: String,
	val friends: List<String>,
	val newFriends: List<String>
)