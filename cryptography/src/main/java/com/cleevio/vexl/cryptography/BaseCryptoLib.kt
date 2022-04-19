package com.cleevio.vexl.cryptography

sealed class BaseCryptoLib {
	companion object {
		// Used to load the 'cryptography' library on application startup.
		init {
			System.loadLibrary("libcryptography")
		}
	}
}