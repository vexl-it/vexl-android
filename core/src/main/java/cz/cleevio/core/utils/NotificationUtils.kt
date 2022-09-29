package cz.cleevio.core.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat

class NotificationUtils constructor(
	private val context: Context
) {
	fun areNotificationsDisabled(): Boolean {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
		} else {
			!NotificationManagerCompat.from(context).areNotificationsEnabled()
		}
	}
}