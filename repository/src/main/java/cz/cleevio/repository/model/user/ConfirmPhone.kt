package cz.cleevio.repository.model.user

import android.os.Parcelable
import cz.cleevio.network.response.user.ConfirmPhoneResponse
import kotlinx.parcelize.Parcelize

@Parcelize
data class ConfirmPhone constructor(
	val verificationId: Long,
	val expirationAt: String
): Parcelable

fun ConfirmPhoneResponse.fromNetwork() = ConfirmPhone(
	verificationId = verificationId,
	expirationAt = expirationAt
)
