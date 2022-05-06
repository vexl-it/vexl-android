package cz.cleevio.network.adapters

import com.cleevio.vexl.cryptography.EciesCryptoLib
import com.cleevio.vexl.cryptography.model.KeyPair
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.network.response.common.EncryptedBigDecimal
import okio.internal.commonToUtf8String
import timber.log.Timber
import java.math.BigDecimal
import java.nio.charset.StandardCharsets

class EncryptedBigDecimalAdapter(
	private val encryptedPreferences: EncryptedPreferenceRepository,
) {

	// decrypting the field with our private key
	@FromJson
	fun fromJson(encryptedData: String): EncryptedBigDecimal {
		Timber.tag("ASDX").d("Decrypting decimal field")
		return EncryptedBigDecimal(BigDecimal(0.0))

		val keyPair = KeyPair(
			privateKey = encryptedPreferences.userPrivateKey,
			publicKey = encryptedPreferences.userPublicKey
		)
		val byteData = encryptedData.toByteArray(StandardCharsets.UTF_8)
		val pubKeyData = keyPair.publicKey.toByteArray(StandardCharsets.UTF_8)
		val privKeyData = keyPair.privateKey.toByteArray(StandardCharsets.UTF_8)
		val decrypted = EciesCryptoLib.decrypt2(
			pubKeyData, pubKeyData.size,
			privKeyData, privKeyData.size,
			byteData, byteData.size
		).commonToUtf8String()
		//val decrypted = EciesCryptoLib.decrypt(keyPair, encryptedData)
		Timber.tag("ASDX").d("value is $decrypted")
		return EncryptedBigDecimal(BigDecimal(decrypted))
	}

	// no encryption when sending the data from app to api, we have to handle it manually
	@ToJson
	fun toJson(value: EncryptedBigDecimal) = value.toString()
}