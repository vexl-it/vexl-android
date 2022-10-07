package cz.cleevio.core.utils

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun <T, V> Iterable<T>.asyncAll(coroutine: suspend (T) -> V): Iterable<V> = coroutineScope {
	this@asyncAll.map { async { coroutine(it) } }.awaitAll()
}