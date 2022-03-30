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
	val picture: Picture?,
	val friends: List<FacebookUserResponse>
)

@JsonClass(generateAdapter = true)
data class Picture constructor(
	val data: PictureData
)

@JsonClass(generateAdapter = true)
data class PictureData constructor(
	val height: Int,
	val width: Int,
	val isSilhouette: Boolean,
	val url: String
)