package com.cleevio.vexl.cryptography

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Assert.assertEquals
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
class AesCryptoInstrumentedTest {

	@Test
	fun testAes() {
		val password = "some password"
		val originalMessage = "some message"

		val passwordArray = password.toByteArray(StandardCharsets.UTF_8)
		val messageArray = password.toByteArray(StandardCharsets.UTF_8)

		val encryptedMessageArray = AesCryptoLib.encrypt(
			passwordArray, passwordArray.size,
			messageArray, passwordArray.size
		)
		val encryptedMessage = encryptedMessageArray.commonToUtf8String()
		Assert.assertNotEquals(originalMessage, encryptedMessage)

		val decryptedMessageArray = AesCryptoLib.decrypt(
			passwordArray, passwordArray.size,
			encryptedMessageArray, encryptedMessageArray.size
		)
		val decryptedMessage = decryptedMessageArray.commonToUtf8String()
		assertEquals(originalMessage, decryptedMessage)
	}
}