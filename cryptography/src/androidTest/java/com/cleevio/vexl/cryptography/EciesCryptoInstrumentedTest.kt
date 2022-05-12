package com.cleevio.vexl.cryptography

import androidx.test.ext.junit.runners.AndroidJUnit4
import okio.internal.commonToUtf8String
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.charset.StandardCharsets

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

		val pubKeyByteArray = keyPair.publicKey.toByteArray(StandardCharsets.UTF_8)
		val privKeyByteArray = keyPair.privateKey.toByteArray(StandardCharsets.UTF_8)
		val originalMessageByteArray = originalMessage.toByteArray(StandardCharsets.UTF_8)

		val enryptedMessage = EciesCryptoLib.encrypt2(
			pubKeyByteArray,
			pubKeyByteArray.size,
			originalMessageByteArray,
			originalMessageByteArray.size
		).commonToUtf8String()
		Assert.assertNotEquals(originalMessage, enryptedMessage)

		val enryptedMessageByteArray = enryptedMessage.toByteArray(StandardCharsets.UTF_8)

		val decryptedMessage = EciesCryptoLib.decrypt2(
			pubKeyByteArray, pubKeyByteArray.size,
			privKeyByteArray, privKeyByteArray.size,
			enryptedMessageByteArray, enryptedMessageByteArray.size
		).commonToUtf8String()
		assertEquals(originalMessage, decryptedMessage)
	}
}