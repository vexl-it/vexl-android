package cz.cleevio.network.agent

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import timber.log.Timber

class UserAgentImpl constructor(
	private val appContext: Context
) : UserAgent {

	override fun getApplicationIdentifier(): String {
		var versionName = "unknown"
		var versionCode = "unknown"
		try {
			val packageInfo: PackageInfo = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
			versionName = packageInfo.versionName
			versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
				packageInfo.longVersionCode.toString()
			} else {
				@Suppress("DEPRECATION")
				packageInfo.versionCode.toString()
			}
		} catch (e: PackageManager.NameNotFoundException) {
			Timber.e(e)
		}

		return StringBuilder().apply {
			append("Cleevio/")
			append(versionName)
			append(" (")
			append(appContext.packageName)
			append("; ")
			append("build: ")
			append(versionCode)
			append("; ")
			append(Build.MANUFACTURER)
			append(Build.MODEL)
			append("/")
			append("Android ")
			append(Build.VERSION.SDK_INT)
			append(")")
		}.toString()
	}
}