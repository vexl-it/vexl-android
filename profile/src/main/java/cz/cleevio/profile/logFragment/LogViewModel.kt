package cz.cleevio.profile.logFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.network.utils.LogData
import cz.cleevio.network.utils.LogUtils
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LogViewModel constructor(
	private val logUtils: LogUtils,
	val encryptedPreferenceRepository: EncryptedPreferenceRepository
) : BaseViewModel() {

	private val logsList = mutableListOf<LogData>()
	private val _logs = MutableStateFlow<List<LogData>>(emptyList())
	val logs = _logs.asStateFlow()

	init {
		viewModelScope.launch {
			logUtils.logFlow.collect {
				logsList.add(0, it)
				_logs.emit(logsList)
			}
		}
	}

	fun changeLogs(checked: Boolean) {
		encryptedPreferenceRepository.logsEnabled = checked
	}
}