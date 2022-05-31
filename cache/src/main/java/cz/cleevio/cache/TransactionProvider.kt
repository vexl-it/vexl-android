package cz.cleevio.cache

import androidx.room.withTransaction

class TransactionProvider(
	private val cleevioDatabase: CleevioDatabase
) {
	suspend fun <R> runAsTransaction(block: suspend () -> R): R = cleevioDatabase.withTransaction(block)
}