package com.cleevio.vexl.cryptography

import com.cleevio.vexl.cryptography.model.KeyPair
import okio.internal.commonToUtf8String
import java.nio.charset.StandardCharsets

object EcdsaCryptoLib : BaseCryptoLib() {

	fun sign(keyPair: KeyPair, data: String): String {

		val publicKeyArray = keyPair.publicKey.toByteArray(StandardCharsets.UTF_8)
		val privateKeyArray = keyPair.privateKey.toByteArray(StandardCharsets.UTF_8)
		val dataArray = data.toByteArray(StandardCharsets.UTF_8)

		val result = sign(
			publicKeyArray, publicKeyArray.size,
			privateKeyArray, privateKeyArray.size,
			dataArray, dataArray.size
		)

		return result.commonToUtf8String()
	}

	fun verify(publicKey: String, data: String, signature: String): Boolean {

		val publicKeyArray = publicKey.toByteArray(StandardCharsets.UTF_8)
		val dataArray = data.toByteArray(StandardCharsets.UTF_8)
		val signatureArray = signature.toByteArray(StandardCharsets.UTF_8)

		return verify(
			publicKeyArray, publicKeyArray.size,
			dataArray, dataArray.size,
			signatureArray, signatureArray.size
		)
	}

	private external fun sign(
		publicKeyArray: ByteArray, publicKeyArrayLen: Int,
		privateKeyArray: ByteArray, privateKeyArrayLen: Int,
		dataArray: ByteArray, dataArrayLen: Int
	): ByteArray

	private external fun verify(
		publicKeyArray: ByteArray, publicKeyArrayLen: Int,
		dataArray: ByteArray, dataArrayLen: Int,
		signatureArray: ByteArray, signatureArrayLen: Int
	): Boolean

}