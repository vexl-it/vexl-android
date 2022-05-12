package com.cleevio.vexl.cryptography

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.charset.StandardCharsets
import okio.internal.commonToUtf8String

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class EcdsaCryptoInstrumentedTest {

	@Test
	fun testEcdsa() {

		val keyPair = KeyPairCryptoLib.generateKeyPair()
		val data = "Some data"
		val dataArray = data.toByteArray(StandardCharsets.UTF_8)

		val signatureArray = EcdsaCryptoLib.sign(
			keyPair.publicKey, keyPair.publicKey.size,
			keyPair.privateKey, keyPair.privateKey.size,
			dataArray, dataArray.size
		)

		val signature = signatureArray.commonToUtf8String()

		Assert.assertNotEquals(data, signature)

		val verified = EcdsaCryptoLib.verify(
			keyPair.publicKey, keyPair.publicKey.size,
			dataArray, dataArray.size,
			signatureArray, signatureArray.size
		)

		Assert.assertTrue(verified)

	}

}