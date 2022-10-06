package cz.cleevio.cache.preferences

import kotlinx.coroutines.flow.MutableStateFlow

interface EncryptedPreferenceRepository {

	var isUserVerified: Boolean
	var userPublicKey: String
	var userPrivateKey: String
	var userCountryCode: String
	var userPhoneNumber: String
	var signature: String
	var hash: String
	var facebookSignature: String
	var facebookHash: String
	var selectedCurrency: String
	var selectedCryptoCurrency: String
	var areScreenshotsAllowed: Boolean
	var numberOfImportedContacts: Int
	var groupCode: Long
	var logsEnabled: Boolean

	val areScreenshotsAllowedFlow: MutableStateFlow<Boolean>
	val selectedCurrencyFlow: MutableStateFlow<String>
	val numberOfImportedContactsFlow: MutableStateFlow<Int>

	fun clearPreferences()
}