package cz.cleevio.network.adapters

import com.cleevio.vexl.cryptography.EciesCryptoLib
import com.cleevio.vexl.cryptography.model.KeyPair
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.network.response.common.EncryptedLocation
import timber.log.Timber

class EncryptedLocationAdapter(
	private val encryptedPreferences: EncryptedPreferenceRepository,
	private val locationAdapter: LocationAdapter
) {

	// decrypting the field with our private key
	@FromJson
	fun fromJson(encryptedData: String): EncryptedLocation {
		Timber.tag("ASDX").d("Decrypting location field $encryptedData")
		val keyPair = KeyPair(
			privateKey = encryptedPreferences.userPrivateKey,
			publicKey = encryptedPreferences.userPublicKey
		)
		val decrypted = EciesCryptoLib.decrypt(keyPair, encryptedData)
		Timber.tag("ASDX").d("value is $decrypted")
		val location = locationAdapter.fromJson(decrypted)
		Timber.tag("ASDX").d("parsed location is $location")
		return EncryptedLocation(location)
	}

	// no encryption when sending the data from app to api, we have to handle it manually
	@ToJson
	fun toJson(value: EncryptedLocation) = value.toString()
}