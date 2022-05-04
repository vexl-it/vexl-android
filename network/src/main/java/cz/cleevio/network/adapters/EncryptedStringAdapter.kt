package cz.cleevio.network.adapters

import com.cleevio.vexl.cryptography.EciesCryptoLib
import com.cleevio.vexl.cryptography.model.KeyPair
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.network.response.common.EncryptedString
import timber.log.Timber

class EncryptedStringAdapter(
	private val encryptedPreferences: EncryptedPreferenceRepository,
) {

	// decrypting the field with our private key
	@FromJson
	fun fromJson(encryptedData: String): EncryptedString {
		Timber.tag("ASDX").d("Decrypting string field $encryptedData")
		val keyPair = KeyPair(
			privateKey = encryptedPreferences.userPrivateKey,
			publicKey = encryptedPreferences.userPublicKey
		)
		//val decrypted = EciesCryptoLib.decrypt2(keyPair, encryptedData.toByteArray(StandardCharsets.UTF_8)).commonToUtf8String()
		val decrypted = EciesCryptoLib.decrypt(keyPair, encryptedData)
		Timber.tag("ASDX").d("value is $decrypted")
		return EncryptedString(decrypted)
	}

	// no encryption when sending the data from app to api, we have to handle it manually
	@ToJson
	fun toJson(value: EncryptedString) = value.toString()
}