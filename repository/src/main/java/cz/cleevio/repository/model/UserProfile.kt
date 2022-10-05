package cz.cleevio.repository.model

data class UserProfile constructor(
	val fullname: String,
	val avatar: String?,
	val avatarBase64: String?
)