package com.cleevio.vexl.cryptography

object EciesCryptoLib : BaseCryptoLib() {

	external fun init()

//	external fun encrypt(publicKey: String, message: String): String

	external fun encrypt2(publicKey: ByteArray, publicKeyLen: Int, message: ByteArray, messageLen: Int): ByteArray

//	external fun decrypt(keys: KeyPair, encodedMessage: String): String

	external fun decrypt2(
		publicKey: ByteArray, publicKeyLen: Int,
		privateKey: ByteArray, privateKeyLen: Int,
		encodedMessage: ByteArray, messageLen: Int
	): ByteArray
}