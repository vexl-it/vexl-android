package com.cleevio.vexl.cryptography

import com.cleevio.vexl.cryptography.model.KeyPair

object EciesCryptoLib : BaseCryptoLib() {

	external fun init()

	external fun encrypt(keys: KeyPair, message: String): String

	external fun decrypt(keys: KeyPair, encodedMessage: String): String

}