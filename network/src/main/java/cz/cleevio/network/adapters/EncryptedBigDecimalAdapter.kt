package cz.cleevio.network.adapters

import com.cleevio.vexl.cryptography.EciesCryptoLib
import com.cleevio.vexl.cryptography.model.KeyPair
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.network.response.common.EncryptedBigDecimal
import java.math.BigDecimal

class EncryptedBigDecimalAdapter(
	private val encryptedPreferences: EncryptedPreferenceRepository,
) {

	// decrypting the field with our private key
	@FromJson
	fun fromJson(encryptedData: String): EncryptedBigDecimal {
		val keyPair = KeyPair(
			privateKey = encryptedPreferences.userPrivateKey,
			publicKey = encryptedPreferences.userPublicKey
		)
		val decrypted = BigDecimal(EciesCryptoLib.decrypt(keyPair, encryptedData))
		return EncryptedBigDecimal(decrypted)
	}

	// no encryption when sending the data from app to api, we have to handle it manually
	@ToJson
	fun toJson(value: EncryptedBigDecimal) = value.toString()
}