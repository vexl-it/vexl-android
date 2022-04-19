package com.cleevio.vexl.cryptography

import com.cleevio.vexl.cryptography.model.KeyPair

object KeyPairCryptoLib : BaseCryptoLib() {

	external fun generateKeyPair(): KeyPair

}