package cz.cleevio.network.cache

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.*

class NetworkCacheImpl constructor(
	private val appContext: Context
) : NetworkCache {

	private val encryptedSharedPreferences by lazy {
		EncryptedSharedPreferences.create(
			appContext,
			ENCRYPTED_FILE,
			MasterKey.Builder(appContext)
				.setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
				.build(),
			EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
			EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
		)
	}

	override var installUUID: String?
		get() {
			val uuid = getNullableStringFromEP(KEY_INSTALL_UUID)
			return if (uuid.isNullOrEmpty()) {
				val generatedUUID = generateInstallUUID()
				putStringToEP(KEY_INSTALL_UUID, generatedUUID)
				generatedUUID
			} else {
				uuid
			}
		}
		set(value) {
			if (value.isNullOrEmpty()) {
				removeEncryptedPreference(KEY_INSTALL_UUID)
			} else {
				putStringToEP(KEY_INSTALL_UUID, value)
			}
		}

	override var accessTokenGeneral: String?
		get() = getNullableStringFromEP(KEY_AUTH_TOKEN_GENERAL)
		set(value) {
			if (value.isNullOrEmpty()) {
				removeEncryptedPreference(KEY_AUTH_TOKEN_GENERAL)
			} else {
				putStringToEP(KEY_AUTH_TOKEN_GENERAL, value)
			}
		}

	override var accessTokenGrant: String?
		get() = getNullableStringFromEP(KEY_AUTH_TOKEN_GRANT)
		set(value) {
			if (value.isNullOrEmpty()) {
				removeEncryptedPreference(KEY_AUTH_TOKEN_GRANT)
			} else {
				putStringToEP(KEY_AUTH_TOKEN_GRANT, value)
			}
		}

	override var refreshToken: String?
		get() = getNullableStringFromEP(KEY_AUTH_TOKEN_REFRESH)
		set(value) {
			if (value.isNullOrEmpty()) {
				removeEncryptedPreference(KEY_AUTH_TOKEN_REFRESH)
			} else {
				putStringToEP(KEY_AUTH_TOKEN_REFRESH, value)
			}
		}

	override var firebaseToken: String?
		get() = getNullableStringFromEP(KEY_AUTH_FIREBASE_TOKEN)
		set(value) {
			if (value.isNullOrEmpty()) {
				removeEncryptedPreference(KEY_AUTH_FIREBASE_TOKEN)
			} else {
				putStringToEP(KEY_AUTH_FIREBASE_TOKEN, value)
			}
		}

	override var firebaseTokenWasSent: Boolean
		get() = getBooleanFromEP(KEY_AUTH_FIREBASE_TOKEN_WAS_SENT, false)
		set(value) {
			putBooleanToEP(KEY_AUTH_FIREBASE_TOKEN_WAS_SENT, value)
		}

	private fun removeEncryptedPreference(key: String) =
		encryptedSharedPreferences.edit().remove(key).apply()

	private fun getNullableStringFromEP(key: String): String? =
		encryptedSharedPreferences.getString(key, null)

	private fun getBooleanFromEP(key: String, defaultValue: Boolean): Boolean =
		encryptedSharedPreferences.getBoolean(key, defaultValue)

	private fun putStringToEP(key: String, value: String?) =
		encryptedSharedPreferences.edit().putString(key, value).apply()

	private fun putBooleanToEP(key: String, value: Boolean) =
		encryptedSharedPreferences.edit().putBoolean(key, value).apply()

	private fun generateInstallUUID(): String =
		UUID.randomUUID().toString()

	companion object {
		private const val ENCRYPTED_FILE = "network-cache-encrypted-prefs"
		private const val KEY_INSTALL_UUID = "install_uuid"
		private const val KEY_AUTH_TOKEN_GENERAL = "auth_token_general"
		private const val KEY_AUTH_TOKEN_GRANT = "auth_token_grant"
		private const val KEY_AUTH_TOKEN_REFRESH = "auth_token_refresh"
		private const val KEY_AUTH_FIREBASE_TOKEN = "auth_firebase_token"
		private const val KEY_AUTH_FIREBASE_TOKEN_WAS_SENT = "auth_firebase_token_was_sent"
	}
}
