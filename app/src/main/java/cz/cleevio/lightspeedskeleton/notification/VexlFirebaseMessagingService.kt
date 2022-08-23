package cz.cleevio.lightspeedskeleton.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import cz.cleevio.lightspeedskeleton.R
import cz.cleevio.lightspeedskeleton.ui.mainActivity.MainActivity
import cz.cleevio.repository.model.contact.ContactKey
import cz.cleevio.repository.model.contact.ContactLevel
import cz.cleevio.repository.repository.chat.ChatRepository
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.group.GroupRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent
import timber.log.Timber

class VexlFirebaseMessagingService : FirebaseMessagingService(), KoinComponent {

	private val chatRepository: ChatRepository by inject()
	private val groupRepository: GroupRepository by inject()
	private val contactRepository: ContactRepository by inject()
	private val coroutineScope = CoroutineScope(Dispatchers.IO)

	override fun onNewToken(token: String) {
		super.onNewToken(token)
		Timber.tag("FIREBASE_TOKEN").d(token)
		coroutineScope.launch {
			chatRepository.saveFirebasePushToken(token)
		}
	}

	override fun onMessageReceived(remoteMessage: RemoteMessage) {
		val title = remoteMessage.data[NOTIFICATION_TITLE]
		val message = remoteMessage.data[NOTIFICATION_BODY]
		val type = remoteMessage.data[NOTIFICATION_TYPE] ?: NOTIFICATION_TYPE_DEFAULT
		val inbox = remoteMessage.data[NOTIFICATION_INBOX]
		val sender = remoteMessage.data[NOTIFICATION_SENDER]
		//extra info for notification about new members in group
		//uuid is groupUuid
		val uuid = remoteMessage.data[NOTIFICATION_UUID]
		//extra into for notification about new FIRST_LEVEL contact
		//publicKey is that contact publicKey
		val publicKey = remoteMessage.data[PUBLIC_KEY]

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
				groupRepository.syncNewMembersInGroup(groupUuid)
			}
		}

		publicKey?.let { publicKey ->
			coroutineScope.launch {
				contactRepository.addNewContact(
					ContactKey(
						key = publicKey,
						level = ContactLevel.FIRST,
						groupUuid = null,
						isUpToDate = false
					)
				)
			}
		}

		val pendingIntent = createPendingIntent(type, inbox, sender)
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
	): NotificationCompat.Builder {
		val notificationManager = NotificationManagerCompat.from(applicationContext)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val notificationChannel = NotificationChannel(
				applicationContext.getString(CHANNEL_ID),
				applicationContext.getString(R.string.notification_channel_id),
				NotificationManager.IMPORTANCE_HIGH
			)
			notificationChannel.setShowBadge(true)
			notificationChannel.description = applicationContext.getString(R.string.notification_channel_id)
			notificationManager.createNotificationChannel(notificationChannel)
		}

		return NotificationCompat.Builder(applicationContext, applicationContext.getString(CHANNEL_ID))
			.setContentTitle(title)
			.setContentText(message)
			.setAutoCancel(true)
			.setContentIntent(pendingIntent)
			.setSmallIcon(R.drawable.ic_vexl_logo)
			.setPriority(NotificationCompat.PRIORITY_MAX)
			.setDefaults(NotificationCompat.DEFAULT_ALL)
	}

	private fun createPendingIntent(type: String, inbox: String?, sender: String?): PendingIntent {
		val intent = Intent(applicationContext, MainActivity::class.java).apply {
			putExtra(NOTIFICATION_TYPE, type)
			putExtra(NOTIFICATION_INBOX, inbox)
			putExtra(NOTIFICATION_SENDER, sender)
		}
		return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
			PendingIntent.getActivity(
				applicationContext,
				CODE,
				intent,
				PendingIntent.FLAG_UPDATE_CURRENT
			)
		} else {
			PendingIntent.getActivity(
				applicationContext,
				CODE,
				intent,
				PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
			)
		}
	}

	companion object {
		const val CHANNEL_ID = R.string.channel_chat_id
		const val NOTIFICATION_TYPE = "type"
		const val NOTIFICATION_LOG_MESSAGE = "log_message"
		private const val NOTIFICATION_TITLE = "title"
		private const val NOTIFICATION_BODY = "body"
		const val NOTIFICATION_INBOX = "inbox"
		const val NOTIFICATION_SENDER = "sender"
		private const val NOTIFICATION_UUID = "group_uuid"
		private const val PUBLIC_KEY = "public_key"

		const val NOTIFICATION_TYPE_DEFAULT = "UNKNOWN"
		private const val CODE = 102_487
	}
}

// TODO Handle app navigation according to the notification received
enum class RemoteNotificationType {
	UNKNOWN,
	MESSAGE,
	REQUEST_REVEAL,
	APPROVE_REVEAL,
	DISAPPROVE_REVEAL,
	REQUEST_MESSAGING,
	APPROVE_MESSAGING,
	DISAPPROVE_MESSAGING,
	DELETE_CHAT
}