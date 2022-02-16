package cz.cleevio.network.data

/**
 * Status of a resource that is provided to the UI.
 */
sealed class Status {
	object NotStarted : Status()
	object Success : Status()
	object Error : Status()
	object Loading : Status()
}