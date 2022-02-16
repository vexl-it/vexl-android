package cz.cleevio.cache.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreRepositoryImpl constructor(
	private val context: Context
) : DataStoreRepository {

	private object PreferencesKeys {
		val IS_USER_LOGGED = booleanPreferencesKey("key_is_user_logged")
	}

	private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFS_DATA_STORE_NAME)

	override val isUserLoggedFlow: Flow<Boolean> = context.dataStore.data
		.map { preferences ->
			preferences[PreferencesKeys.IS_USER_LOGGED] ?: false
		}

	override suspend fun updateIsUserLogged(isUserLogged: Boolean) {
		context.dataStore.edit { preferences ->
			preferences[PreferencesKeys.IS_USER_LOGGED] = isUserLogged
		}
	}

	companion object {
		private const val PREFS_DATA_STORE_NAME = "Cleevio-datastore-preferences"
	}
}
