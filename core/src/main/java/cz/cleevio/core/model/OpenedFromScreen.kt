package cz.cleevio.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class OpenedFromScreen constructor(
	val screen: String
) : Parcelable {
	UNKNOWN(""),
	PROFILE("profile"),
	ONBOARDING("onboarding")
}