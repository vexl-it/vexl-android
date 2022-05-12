package com.cleevio.vexl.cryptography

import com.cleevio.vexl.cryptography.model.KeyPair

object EcdsaCryptoLib : BaseCryptoLib() {

	external fun sign(
		publicKeyArray: ByteArray, publicKeyArrayLen: Int,
		privateKeyArray: ByteArray, privateKeyArrayLen: Int,
		dataArray: ByteArray, dataArrayLen: Int
	): ByteArray

	external fun verify(
		publicKeyArray: ByteArray, publicKeyArrayLen: Int,
		dataArray: ByteArray, dataArrayLen: Int,
		signatureArray: ByteArray, signatureArrayLen: Int,
	): Boolean

}