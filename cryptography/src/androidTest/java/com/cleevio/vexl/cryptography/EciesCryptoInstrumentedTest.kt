package com.cleevio.vexl.cryptography

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class EciesCryptoInstrumentedTest {

	@Test
	fun testEcies() {
		val originalMessage = "some message"
		val keyPair = KeyPairCryptoLib.generateKeyPair()

		EciesCryptoLib.init()

		val encryptedMessage = EciesCryptoLib.encrypt(keyPair.publicKey, originalMessage)
		Assert.assertNotEquals(originalMessage, encryptedMessage)

		val decryptedMessage = EciesCryptoLib.decrypt(keyPair, encryptedMessage)
		assertEquals(originalMessage, decryptedMessage)
	}

}