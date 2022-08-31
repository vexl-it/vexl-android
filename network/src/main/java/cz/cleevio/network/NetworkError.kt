package cz.cleevio.network

import cz.cleevio.network.data.ErrorIdentification
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class NetworkError {
	private val _error = Channel<ErrorIdentification?>(Channel.CONFLATED)
	val error = _error.receiveAsFlow()

	private val _logout = Channel<Unit>(Channel.CONFLATED)
	val logout = _logout.receiveAsFlow()

	fun sendError(error: ErrorIdentification) {
		_error.trySend(error)
	}

	fun sendLogout() {
		_logout.trySend(Unit)
	}
}