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
class KeyPairCryptoInstrumentedTest {

	@Test
	fun useAppContext() {

		val keyPair = KeyPairCryptoLib.generateKeyPair()

		Assert.assertTrue(
			keyPair.privateKey.contains("BEGIN PRIVATE KEY") &&
				keyPair.publicKey.contains("BEGIN PUBLIC KEY")
		)

	}

}