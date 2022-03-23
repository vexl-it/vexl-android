package cz.cleevio.cache.preferences

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

@Suppress("Unused")
class EncryptedPreferenceRepositoryImpl constructor(
	private val context: Context
) : EncryptedPreferenceRepository {

	private val encryptedSharedPreferences by lazy {
		EncryptedSharedPreferences.create(
			context,
			ENCRYPTED_FILE,
			MasterKey.Builder(context)
				.setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
				.build(),
			EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
			EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
		)
	}

	override var isUserVerified: Boolean
		get() = getBooleanFromEP(KEY_IS_USER_VERIFIED, false)
		set(value) {
			putBooleanToEP(KEY_IS_USER_VERIFIED, value)
		}

	override var userPublicKey: String
		get() = getStringFromEP(KEY_USER_PUBLIC_KEY, "")
		set(value) {
			putStringToEP(KEY_USER_PUBLIC_KEY, value)
		}

	override var userPrivateKey: String
		get() = getStringFromEP(KEY_USER_PRIVATE_KEY, "")
		set(value) {
			putStringToEP(KEY_USER_PRIVATE_KEY, value)
		}

	override var signature: String
		get() = getStringFromEP(KEY_USER_SIGNATURE, "")
		set(value) {
			putStringToEP(KEY_USER_SIGNATURE, value)
		}

	override var hash: String
		get() = getStringFromEP(KEY_HASH, "")
		set(value) {
			putStringToEP(KEY_HASH, value)
		}

	private fun removeFromEP(key: String) =
		encryptedSharedPreferences.edit().remove(key).apply()

	private fun getBooleanFromEP(key: String, defaultValue: Boolean): Boolean =
		encryptedSharedPreferences.getBoolean(key, defaultValue)

	private fun getNullableStringFromEP(key: String, defaultValue: String?): String? =
		encryptedSharedPreferences.getString(key, defaultValue)

	private fun getNullableStringFromEP(key: String): String? =
		encryptedSharedPreferences.getString(key, null)

	private fun getStringFromEP(key: String, defaultValue: String): String =
		encryptedSharedPreferences.getString(key, defaultValue) ?: ""

	private fun getIntFromEP(key: String, defaultValue: Int): Int =
		encryptedSharedPreferences.getInt(key, defaultValue)

	private fun getLongFromEP(key: String, defaultValue: Long): Long =
		encryptedSharedPreferences.getLong(key, defaultValue)

	private fun putBooleanToEP(key: String, value: Boolean) =
		encryptedSharedPreferences.edit().putBoolean(key, value).apply()

	private fun putStringToEP(key: String, value: String?) =
		encryptedSharedPreferences.edit().putString(key, value).apply()

	private fun putIntToEP(key: String, value: Int) =
		encryptedSharedPreferences.edit().putInt(key, value).apply()

	private fun putLongToEP(key: String, value: Long) =
		encryptedSharedPreferences.edit().putLong(key, value).apply()

	companion object {
		private const val ENCRYPTED_FILE = "encrypted-prefs-repository"
		private const val KEY_IS_USER_VERIFIED = "is_user_verified"
		private const val KEY_USER_PUBLIC_KEY = "user_public_key"
		private const val KEY_USER_PRIVATE_KEY = "user_private_key"
		private const val KEY_USER_SIGNATURE = "user_signature"
		private const val KEY_HASH = "hash"
	}
}