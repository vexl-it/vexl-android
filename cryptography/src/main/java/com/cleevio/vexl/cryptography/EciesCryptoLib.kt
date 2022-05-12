package com.cleevio.vexl.cryptography

object EciesCryptoLib : BaseCryptoLib() {

	external fun init()

	external fun encrypt(
		publicKey: ByteArray, publicKeyLen: Int,
		message: ByteArray, messageLen: Int
	): ByteArray

	external fun decrypt(
		publicKey: ByteArray, publicKeyLen: Int,
		privateKey: ByteArray, privateKeyLen: Int,
		encodedMessage: ByteArray, messageLen: Int
	): ByteArray
}