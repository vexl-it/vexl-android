package com.cleevio.vexl.cryptography

object AesCryptoLib : BaseCryptoLib() {

	external fun encrypt(password: String, message: String): String

	external fun decrypt(password: String, cipher: String): String

}