package cz.cleevio.repository.model.contact

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommonFriend(
	val contactHash: String,
	val id: Long? = null,
	val name: String? = null,
	val type: String? = null,
	val userAvatar: String? = null
) : Parcelable