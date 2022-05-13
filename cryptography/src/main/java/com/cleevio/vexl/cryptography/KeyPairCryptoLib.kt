package com.cleevio.vexl.cryptography

import com.cleevio.vexl.cryptography.model.KeyPair
import com.cleevio.vexl.cryptography.model.KeyPairInternal
import okio.internal.commonToUtf8String

object KeyPairCryptoLib : BaseCryptoLib() {

	fun generateKeyPair(): KeyPair {
		val keyPair = generateKeyPairInternal()
		return KeyPair(
			privateKey = keyPair.privateKey.commonToUtf8String(),
			publicKey = keyPair.publicKey.commonToUtf8String()
		)
	}

	private external fun generateKeyPairInternal(): KeyPairInternal

}