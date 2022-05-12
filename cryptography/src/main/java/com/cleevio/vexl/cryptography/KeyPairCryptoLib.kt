package com.cleevio.vexl.cryptography

import com.cleevio.vexl.cryptography.model.KeyPairArrays

object KeyPairCryptoLib : BaseCryptoLib() {

	external fun generateKeyPair(): KeyPairArrays

}