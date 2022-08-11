package cz.cleevio.repository.model.user

import android.os.Parcelable
import cz.cleevio.cache.entity.UserEntity
import cz.cleevio.network.response.user.UserResponse
import kotlinx.parcelize.Parcelize

@Parcelize
data class User constructor(
	val id: Long?,
	val username: String,
	val anonymousUsername: String?,
	val avatar: String?,
	val anonymousAvatarImageIndex: Int?,
	val publicKey: String,
	val finishedOnboarding: Boolean
) : Parcelable

fun UserResponse.fromNetwork() = User(
	id = null,
	username = username,
	anonymousUsername = null,
	avatar = avatar,
	anonymousAvatarImageIndex = null,
	publicKey = publicKey,
	finishedOnboarding = false
)

fun UserEntity.fromDao() = User(
	id = id,
	username = username,
	anonymousUsername = anonymousUsername,
	avatar = avatar,
	anonymousAvatarImageIndex = anonymousAvatarImageIndex,
	publicKey = publicKey,
	finishedOnboarding = finishedOnboarding
)