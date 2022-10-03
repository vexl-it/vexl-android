package cz.cleevio.core.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class LogUtils {

	//val xxx = BlockingQueue
	//private val logs: MutableList<LogData> = mutableListOf()
	//private val logsQ: Queue<LogData> = LinkedList<LogData>()

	private val _logFlow = MutableSharedFlow<LogData>(replay = 100)
	val logFlow = _logFlow.asSharedFlow()

	fun addLog(data: LogData) {
//		logs.add(data)
		_logFlow.tryEmit(data)
//
//		logsQ.add(data)
	}
}

data class LogData(
	val timestamp: Long = System.currentTimeMillis(),
	val log: String
)