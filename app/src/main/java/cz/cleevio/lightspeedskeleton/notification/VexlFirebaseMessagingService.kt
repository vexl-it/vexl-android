package cz.cleevio.lightspeedskeleton.notification

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import cz.cleevio.lightspeedskeleton.R
import cz.cleevio.lightspeedskeleton.ui.mainActivity.MainActivity
import cz.cleevio.network.data.Status
import cz.cleevio.repository.repository.chat.ChatRepository
import cz.cleevio.repository.repository.group.GroupRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent
import timber.log.Timber

class VexlFirebaseMessagingService : FirebaseMessagingService(), KoinComponent {

	private val chatRepository: ChatRepository by inject()
	private val groupRepository: GroupRepository by inject()
	private val coroutineScope = CoroutineScope(Dispatchers.IO)

	override fun onNewToken(token: String) {
		super.onNewToken(token)
		Timber.tag("FIREBASE_TOKEN").d(token)
		coroutineScope.launch {
			chatRepository.saveFirebasePushToken(token)
		}
	}

	@OptIn(DelicateCoroutinesApi::class)
	override fun onMessageReceived(remoteMessage: RemoteMessage) {
		val title = remoteMessage.data[NOTIFICATION_TITLE]
		val message = remoteMessage.data[NOTIFICATION_BODY]
		val type = remoteMessage.data[NOTIFICATION_TYPE] ?: NOTIFICATION_TYPE_DEFAULT
		val inbox = remoteMessage.data[NOTIFICATION_INBOX]
		val uuid = remoteMessage.data[NOTIFICATION_UUID]

		Timber.tag("FIREBASE").d("$inbox")

		//todo: do some custom stuff here, check notification type, check DB, display or not
		//there will be more stuff when we implement group functionality
		inbox?.let { inboxPublicKey ->
			coroutineScope.launch {
				chatRepository.syncMessages(inboxPublicKey)
			}
		}

		uuid?.let { groupUuid ->
			coroutineScope.launch {
				//todo: check if we have any offers created for this group

				//if we do, continue
				val response = groupRepository.syncNewMembersInGroup(groupUuid)
				//TODO: encrypt all offers for these new members and send it to BE
				//fixme: when BE has new EP ready for adding extra private-parts to offer
				when (response.status) {
					is Status.Success -> {
						//just debug
						response.data?.map {
							Timber.d("newMember: $it")
						}
					}
				}
			}
		}

		val intent = Intent(applicationContext, MainActivity::class.java)
		intent.putExtra(NOTIFICATION_TYPE, type)
		intent.putExtra(NOTIFICATION_LOG_MESSAGE, "Notification $type with title $title clicked")
		val pendingIntent = PendingIntent.getActivity(
			applicationContext,
			CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
		)

		if (title != null && message != null) {
			coroutineScope.launch {
				showNotification(
					createNotificationBuilder(
						title = title,
						message = message,
						pendingIntent = pendingIntent
					)
				)
			}
		}

		super.onMessageReceived(remoteMessage)
	}

	private fun showNotification(notificationBuilder: NotificationCompat.Builder?) {
		with(NotificationManagerCompat.from(applicationContext)) {
			notificationBuilder?.let {
				notify(System.currentTimeMillis().toInt(), it.build())
			}
		}
	}

	private fun createNotificationBuilder(
		title: String,
		message: String,
		pendingIntent: PendingIntent
	): NotificationCompat.Builder? {
		return NotificationCompat.Builder(applicationContext, applicationContext.getString(CHANNEL_ID))
			.setContentTitle(title)
			.setContentText(message)
			.setAutoCancel(true)
			.setContentIntent(pendingIntent)
			//todo: check icon
			.setSmallIcon(R.drawable.ic_chat)
			.setPriority(NotificationCompat.PRIORITY_MAX)
			.setDefaults(NotificationCompat.DEFAULT_ALL)
	}

	companion object {
		const val CHANNEL_ID = R.string.channel_chat_id
		const val NOTIFICATION_TYPE = "type"
		const val NOTIFICATION_LOG_MESSAGE = "log_message"
		private const val NOTIFICATION_TITLE = "title"
		private const val NOTIFICATION_BODY = "body"
		private const val NOTIFICATION_INBOX = "inbox"
		private const val NOTIFICATION_UUID = "uuid"

		const val NOTIFICATION_TYPE_DEFAULT = "UNKNOWN"
		private const val CODE = 102_487
	}
}

//todo: change to proper types as soon as BE ready
enum class RemoteNotificationType {
	UNKNOWN, TEST, DAILY_DRAW, WEEKLY_DRAW, WITHDRAW_UPDATED, CONTEST_DRAW, REFERRAL_BONUS
}