package cz.cleevio.lightspeedskeleton.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cz.cleevio.core.utils.NavMainGraphModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class VexlBroadcastReceiver : BroadcastReceiver(), KoinComponent {

	private val navGraphModel by inject<NavMainGraphModel>()

	@Suppress("GlobalCoroutineUsage")
	override fun onReceive(context: Context?, intent: Intent?) {
		GlobalScope.launch(Dispatchers.Default) {
			navGraphModel.navigateToGraph(NavMainGraphModel.NavGraph.Main)

			intent?.let { intent ->
				val type = RemoteNotificationType.valueOf(
					intent.extras?.getString(VexlFirebaseMessagingService.NOTIFICATION_TYPE, RemoteNotificationType.UNKNOWN.name)
						?: RemoteNotificationType.UNKNOWN.name
				)
				val inboxKey = intent.extras?.getString(VexlFirebaseMessagingService.NOTIFICATION_INBOX) ?: ""
				val senderKey = intent.extras?.getString(VexlFirebaseMessagingService.NOTIFICATION_SENDER_KEY) ?: ""

				when (type) {
					RemoteNotificationType.MESSAGE,
					RemoteNotificationType.REQUEST_REVEAL,
					RemoteNotificationType.APPROVE_REVEAL,
					RemoteNotificationType.DISAPPROVE_REVEAL,
					RemoteNotificationType.APPROVE_MESSAGING -> {
						navGraphModel.navigateToGraph(
							NavMainGraphModel.NavGraph.ChatDetail(inboxKey = inboxKey, senderKey = senderKey)
						)
					}
					RemoteNotificationType.REQUEST_MESSAGING,
					RemoteNotificationType.DISAPPROVE_MESSAGING -> {
						navGraphModel.navigateToGraph(
							NavMainGraphModel.NavGraph.ChatRequests
						)
					}
					RemoteNotificationType.DELETE_CHAT -> {
						navGraphModel.navigateToGraph(
							NavMainGraphModel.NavGraph.ChatList
						)
					}
					else -> {
						navGraphModel.navigateToGraph(
							NavMainGraphModel.NavGraph.Main
						)
					}
				}
			}
		}
	}
}