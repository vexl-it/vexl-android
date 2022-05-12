package com.cleevio.vexl.cryptography

object AesCryptoLib : BaseCryptoLib() {

	external fun encrypt(
		passwordArray: ByteArray, passwordArrayLen: Int,
		messageArray: ByteArray, messageArrayLen: Int
	): ByteArray

	external fun decrypt(
		passwordArray: ByteArray, passwordArrayLen: Int,
		cipherArray: ByteArray, cipherArrayLen: Int
	): ByteArray

}