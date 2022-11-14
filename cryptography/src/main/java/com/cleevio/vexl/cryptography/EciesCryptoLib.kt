package com.cleevio.vexl.cryptography

import com.cleevio.vexl.cryptography.model.KeyPair
import okio.internal.commonToUtf8String
import java.nio.charset.StandardCharsets

object EciesCryptoLib : BaseCryptoLib() {

	external fun init()

	fun encrypt(publicKey: String, message: String): String? {

		val publicKeyArray = publicKey.toByteArray(StandardCharsets.UTF_8)
		val messageArray = message.toByteArray(StandardCharsets.UTF_8)

		val result = encrypt(
			publicKeyArray, publicKeyArray.size,
			messageArray, messageArray.size
		)

		return result?.commonToUtf8String()
	}

	fun decrypt(keyPair: KeyPair, encodedMessage: String): String {

		val publicKeyArray = keyPair.publicKey.toByteArray(StandardCharsets.UTF_8)
		val privateKeyArray = keyPair.privateKey.toByteArray(StandardCharsets.UTF_8)
		val encodedMessageArray = encodedMessage.toByteArray(StandardCharsets.UTF_8)

		val result = decrypt(
			publicKeyArray, publicKeyArray.size,
			privateKeyArray, privateKeyArray.size,
			encodedMessageArray, encodedMessageArray.size
		)

		return result.commonToUtf8String()
	}

	private external fun encrypt(
		publicKey: ByteArray, publicKeyLen: Int,
		message: ByteArray, messageLen: Int
	): ByteArray?

	private external fun decrypt(
		publicKey: ByteArray, publicKeyLen: Int,
		privateKey: ByteArray, privateKeyLen: Int,
		encodedMessage: ByteArray, messageLen: Int
	): ByteArray
}