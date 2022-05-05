package cz.cleeevio.vexl.contacts.importContactsFragment

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