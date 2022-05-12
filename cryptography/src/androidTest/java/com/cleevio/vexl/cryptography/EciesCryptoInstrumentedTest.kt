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

		val originalMessageByteArray = originalMessage.toByteArray(StandardCharsets.UTF_8)

		val encryptedMessageArray = EciesCryptoLib.encrypt(
			keyPair.publicKey, keyPair.publicKey.size,
			originalMessageByteArray, originalMessageByteArray.size
		)

		val encryptedMessaget = encryptedMessageArray.commonToUtf8String()
		Assert.assertNotEquals(originalMessage, encryptedMessaget)

		val enryptedMessageByteArray = encryptedMessaget.toByteArray(StandardCharsets.UTF_8)

		val decryptedMessageArray = EciesCryptoLib.decrypt(
			keyPair.publicKey, keyPair.publicKey.size,
			keyPair.privateKey, keyPair.privateKey.size,
			enryptedMessageByteArray, enryptedMessageByteArray.size
		)

		val decryptedMessage = decryptedMessageArray.commonToUtf8String()
		assertEquals(originalMessage, decryptedMessage)
	}
}