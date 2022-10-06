package cz.cleevio.network.utils

import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

const val LOG_CAPACITY_LIMIT = 100

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

data class LogData(
	val timestamp: Long = System.currentTimeMillis(),
	val log: String
)