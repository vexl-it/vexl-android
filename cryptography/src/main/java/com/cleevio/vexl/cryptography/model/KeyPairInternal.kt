package com.cleevio.vexl.cryptography.model

data class KeyPairInternal(
	var privateKey: ByteArray,
	var publicKey: ByteArray
)