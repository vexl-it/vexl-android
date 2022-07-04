package cz.cleevio.repository.model.contact

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommonFriend(
	val contactHash: String,
	val contact: BaseContact? = null
) : Parcelable