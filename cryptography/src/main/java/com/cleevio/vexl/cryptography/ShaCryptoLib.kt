package com.cleevio.vexl.cryptography

object ShaCryptoLib : BaseCryptoLib() {

	external fun hash(data: String, dataLength: Int): String

}