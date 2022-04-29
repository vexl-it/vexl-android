package com.cleevio.vexl.cryptography

import com.cleevio.vexl.cryptography.model.KeyPair

object EcdsaCryptoLib : BaseCryptoLib() {

	external fun sign(keys: KeyPair, data: String): String

	external fun verify(publicKey: String, data: String, signature: String): Boolean

}