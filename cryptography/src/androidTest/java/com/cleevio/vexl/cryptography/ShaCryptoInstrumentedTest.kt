package com.cleevio.vexl.cryptography

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.charset.StandardCharsets

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ShaCryptoInstrumentedTest {

	@Test
	fun useAppContext() {
		val originalMessage = "some message"

		val messageArray = originalMessage.toByteArray(StandardCharsets.UTF_8)

		val hashedMessage = ShaCryptoLib.hash(messageArray, messageArray.size)

		Assert.assertNotEquals(originalMessage, hashedMessage)
	}
}