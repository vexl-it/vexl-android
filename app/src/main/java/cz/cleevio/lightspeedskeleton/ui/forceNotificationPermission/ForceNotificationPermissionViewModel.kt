package cz.cleevio.lightspeedskeleton.ui.forceNotificationPermission

import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.core.utils.NotificationUtils
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class ForceNotificationPermissionViewModel constructor(
	val navMainGraphModel: NavMainGraphModel,
	val notificationUtils: NotificationUtils
) : BaseViewModel() {

	private val hasPermissionsChannel = Channel<Boolean>(Channel.CONFLATED)
	val hasPermissionsEvent = hasPermissionsChannel.receiveAsFlow()

	fun updateHasPostNotificationsPermissions(hasPermissions: Boolean) {
		hasPermissionsChannel.trySend(hasPermissions)
	}
}