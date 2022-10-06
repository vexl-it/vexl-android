package cz.cleevio.network.utils

import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

const val LOG_CAPACITY_LIMIT = 100
const val TO_SECONDS = 1000L
val timestampFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

class LogUtils constructor(
	val encryptedPreferenceRepository: EncryptedPreferenceRepository
) {
	private val _logFlow = MutableSharedFlow<LogData>(replay = LOG_CAPACITY_LIMIT)
	val logFlow = _logFlow.asSharedFlow()

	fun addLog(data: LogData) {
		if (encryptedPreferenceRepository.logsEnabled) {
			_logFlow.tryEmit(data)
		}
	}
}

fun Long.toTimestampDisplay(): String {
	val dateTime = Instant.ofEpochSecond(this / TO_SECONDS)
		.atZone(ZoneId.systemDefault())
		.toLocalDateTime()
	return timestampFormat.format(dateTime)
}

data class LogData(
	val timestamp: Long = System.currentTimeMillis(),
	val timestampDisplay: String = timestamp.toTimestampDisplay(),
	val log: String
)