package cz.cleevio.network

import cz.cleevio.network.data.ErrorIdentification
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class NetworkError {
	private val _error = Channel<ErrorIdentification?>(Channel.CONFLATED)
	val error = _error.receiveAsFlow()

	fun sendError(error: ErrorIdentification) {
		_error.trySend(error)
	}
}