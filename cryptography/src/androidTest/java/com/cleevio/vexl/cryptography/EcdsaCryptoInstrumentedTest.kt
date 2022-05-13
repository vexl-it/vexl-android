package com.cleevio.vexl.cryptography

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

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

		val signature = EcdsaCryptoLib.sign(keyPair, data)

		Assert.assertNotEquals(data, signature)

		val verified = EcdsaCryptoLib.verify(keyPair.publicKey, data, signature)

		Assert.assertTrue(verified)

	}

}