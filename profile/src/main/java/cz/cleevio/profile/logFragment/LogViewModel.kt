package cz.cleevio.profile.logFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.network.utils.LogData
import cz.cleevio.network.utils.LogUtils
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class LogViewModel constructor(
	private val logUtils: LogUtils
) : BaseViewModel() {

	private val logsList = mutableListOf<LogData>()
	private val _logs = MutableSharedFlow<List<LogData>>(replay = 1)
	val logs = _logs.asSharedFlow()

	init {
		viewModelScope.launch {
			logUtils.logFlow.collect {
				logsList.add(0, it)
				_logs.emit(logsList)
			}
		}
	}
}