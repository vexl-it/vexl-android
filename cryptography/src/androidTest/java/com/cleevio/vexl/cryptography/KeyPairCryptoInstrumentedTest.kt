package com.cleevio.vexl.cryptography

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class KeyPairCryptoInstrumentedTest {

	@Test
	fun testKeyPair() {

		val keyPair = KeyPairCryptoLib.generateKeyPair()

		Assert.assertTrue(
			String(Base64.getDecoder().decode(keyPair.privateKey)).contains("BEGIN PRIVATE KEY") &&
				String(Base64.getDecoder().decode(keyPair.publicKey)).contains("BEGIN PUBLIC KEY")
		)

	}

}