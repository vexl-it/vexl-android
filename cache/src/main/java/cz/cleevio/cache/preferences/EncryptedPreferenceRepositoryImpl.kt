package cz.cleevio.cache.preferences

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow

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

	override var facebookSignature: String
		get() = getStringFromEP(KEY_USER_FACEBOOK_SIGNATURE, "")
		set(value) {
			putStringToEP(KEY_USER_FACEBOOK_SIGNATURE, value)
		}

	override var facebookHash: String
		get() = getStringFromEP(KEY_FACEBOOK_HASH, "")
		set(value) {
			putStringToEP(KEY_FACEBOOK_HASH, value)
		}

	override var selectedCurrency: String
		get() = getStringFromEP(KEY_SELECTED_CURRENCY, "CZK")
		set(value) {
			putStringToEP(KEY_SELECTED_CURRENCY, value)
			selectedCurrencyFlow.tryEmit(value)
		}

	override var selectedCryptoCurrency: String
		get() = getStringFromEP(KEY_SELECTED_CRYPTO_CURRENCY, "")
		set(value) {
			putStringToEP(KEY_SELECTED_CRYPTO_CURRENCY, value)
		}

	override var areScreenshotsAllowed: Boolean
		get() = getBooleanFromEP(ARE_SCREENSHOTS_ALLOWED, true)
		set(value) {
			putBooleanToEP(ARE_SCREENSHOTS_ALLOWED, value)
			areScreenshotsAllowedFlow.tryEmit(value)
		}

	override val areScreenshotsAllowedFlow: MutableStateFlow<Boolean> = MutableStateFlow(areScreenshotsAllowed)
	override val selectedCurrencyFlow: MutableStateFlow<String> = MutableStateFlow(selectedCurrency)

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
		private const val KEY_USER_FACEBOOK_SIGNATURE = "user_facebook_signature"
		private const val KEY_FACEBOOK_HASH = "facebook_hash"
		private const val KEY_SELECTED_CURRENCY = "selected_currency"
		private const val KEY_SELECTED_CRYPTO_CURRENCY = "selected_crypto_currency"
		private const val ARE_SCREENSHOTS_ALLOWED = "are_screenshots_allowed"
	}
}