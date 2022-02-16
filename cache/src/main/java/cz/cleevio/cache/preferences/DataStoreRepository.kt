package cz.cleevio.cache.preferences

import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {

	val isUserLoggedFlow: Flow<Boolean>

	suspend fun updateIsUserLogged(isUserLogged: Boolean)
}