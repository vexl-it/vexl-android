package com.cleevio.vexl.cryptography

import com.cleevio.vexl.cryptography.model.KeyPair

object EciesCryptoLib : BaseCryptoLib() {

	external fun init()

	external fun encrypt(publicKey: String, message: String): String

	external fun encrypt2(publicKey: ByteArray, message: ByteArray): ByteArray

	external fun decrypt(keys: KeyPair, encodedMessage: String): String

	external fun decrypt2(keys: KeyPair, encodedMessage: ByteArray): ByteArray
}