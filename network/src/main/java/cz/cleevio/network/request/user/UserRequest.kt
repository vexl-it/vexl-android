package cz.cleevio.network.request.user

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class UserRequest constructor(
	val username: String?,
	val avatar: UserAvatar?
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class UserAvatar constructor(
	val extension: String,
	val data: String
) : Parcelable