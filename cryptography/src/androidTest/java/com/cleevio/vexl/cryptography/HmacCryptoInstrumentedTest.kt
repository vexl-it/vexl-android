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
class HmacCryptoInstrumentedTest {

	@Test
	fun testHmac() {

		val data = "Some data"
		val password = "Some super strong password"

		val digest = HmacCryptoLib.digest(password, data)

		Assert.assertNotEquals(data, digest)

		val verified = HmacCryptoLib.verify(password, data, digest)

		Assert.assertTrue(verified)

	}

}