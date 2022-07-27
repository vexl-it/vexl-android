package cz.cleevio.vexl.lightbase.core.utils

import androidx.fragment.app.FragmentActivity

object PermissionResolver {

	fun resolve(
		activity: FragmentActivity,
		permissions: Map<String, Boolean>,
		allGranted: () -> Unit,
		denied: (() -> Unit)? = null,
		permanentlyDenied: (() -> Unit)? = null
	) {
		when {
			permissions.values.all { it } -> allGranted.invoke()
			permissions.any {
				!it.value && !activity.shouldShowRequestPermissionRationale(it.key)
			} -> permanentlyDenied?.invoke()
			else -> denied?.invoke()
		}
	}
}
