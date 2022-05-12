package com.cleevio.vexl.cryptography

object ShaCryptoLib : BaseCryptoLib() {

	external fun hash(dataArray: ByteArray, dataArrayLen: Int): ByteArray

}