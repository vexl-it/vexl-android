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
class AesCryptoInstrumentedTest {

	@Test
	fun testAes() {
		val password = "some password"
		val originalMessage = "some message"

		val enryptedMessage = AesCryptoLib.encrypt(password, originalMessage)
		Assert.assertNotEquals(originalMessage, enryptedMessage)

		val decryptedMessage = AesCryptoLib.decrypt(password, enryptedMessage)
		assertEquals(originalMessage, decryptedMessage)
	}
}