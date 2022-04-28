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
class ShaCryptoInstrumentedTest {

	@Test
	fun useAppContext() {
		val originalMessage = "some message"

		val hashedMessage = ShaCryptoLib.hash(originalMessage, originalMessage.length)

		Assert.assertNotEquals(originalMessage, hashedMessage)
	}
}