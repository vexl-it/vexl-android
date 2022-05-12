package com.cleevio.vexl.cryptography.model

data class KeyPair(
	var privateKey: String,
	var publicKey: String
)

data class KeyPairArrays(
	var privateKey: ByteArray,
	var publicKey: ByteArray
)