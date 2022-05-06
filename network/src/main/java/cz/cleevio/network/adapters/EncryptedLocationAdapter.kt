package cz.cleevio.network.adapters

import com.cleevio.vexl.cryptography.EciesCryptoLib
import com.cleevio.vexl.cryptography.model.KeyPair
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.network.response.common.EncryptedLocation
import cz.cleevio.network.response.offer.LocationResponse
import okio.internal.commonToUtf8String
import timber.log.Timber
import java.math.BigDecimal
import java.nio.charset.StandardCharsets

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
		Timber.tag("ASDX").d("with keyPair $keyPair")
		Timber.tag("ASDX").d("data length: ${encryptedData.length}")
		val byteData = encryptedData.toByteArray(StandardCharsets.UTF_8)
		val pubKeyData = keyPair.publicKey.toByteArray(StandardCharsets.UTF_8)
		val privKeyData = keyPair.privateKey.toByteArray(StandardCharsets.UTF_8)
		Timber.tag("ASDX").d("data size: ${byteData.size}")
		val decrypted = EciesCryptoLib.decrypt2(
			pubKeyData, pubKeyData.size,
			privKeyData, privKeyData.size,
			byteData, byteData.size
		).commonToUtf8String()
		//val decrypted = EciesCryptoLib.decrypt(keyPair, encryptedData)
		Timber.tag("ASDX").d("value is $decrypted")
		//val location = locationAdapter.fromJson(decrypted)
		//Timber.tag("ASDX").d("parsed location is $location")
		//return EncryptedLocation(location)
		val fakeData = LocationResponse(
			BigDecimal(0.0),
			BigDecimal(0.0),
			BigDecimal(0.0)
		)
		return EncryptedLocation(fakeData)
	}

	// no encryption when sending the data from app to api, we have to handle it manually
	@ToJson
	fun toJson(value: EncryptedLocation) = value.toString()
}