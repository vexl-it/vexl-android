package com.cleevio.vexl.cryptography

class AesCryptoLib {

	external fun encrypt(password: String, message: String): String

	companion object {
		// Used to load the 'cryptography' library on application startup.
		init {
			System.loadLibrary("libcryptography")
		}
	}
}