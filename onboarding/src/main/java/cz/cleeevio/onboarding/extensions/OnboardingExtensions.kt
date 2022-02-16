package cz.cleeevio.onboarding.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent

@SuppressLint("QueryPermissionsNeeded")
fun Activity.navigateToEmailApp() {
	val intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_EMAIL)
	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
	if (intent.resolveActivity(this.packageManager) != null) {
		startActivity(Intent.createChooser(intent, ""))
	}
}