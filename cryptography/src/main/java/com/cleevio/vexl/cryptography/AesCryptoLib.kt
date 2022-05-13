package com.cleevio.vexl.cryptography

import okio.internal.commonToUtf8String
import java.nio.charset.StandardCharsets

object AesCryptoLib : BaseCryptoLib() {

	fun encrypt(password: String, message: String): String {

		val passwordArray = password.toByteArray(StandardCharsets.UTF_8)
		val messageArray = message.toByteArray(StandardCharsets.UTF_8)

		val result = encrypt(
			passwordArray, passwordArray.size,
			messageArray, messageArray.size
		)

		return result.commonToUtf8String()

	}

	fun decrypt(password: String, encryptedMessage: String): String {

		val passwordArray = password.toByteArray(StandardCharsets.UTF_8)
		val encryptedMessageArray = encryptedMessage.toByteArray(StandardCharsets.UTF_8)

		val result = decrypt(
			passwordArray, passwordArray.size,
			encryptedMessageArray, encryptedMessageArray.size
		)

		return result.commonToUtf8String()

	}

	private external fun encrypt(
		passwordArray: ByteArray, passwordArrayLen: Int,
		messageArray: ByteArray, messageArrayLen: Int
	): ByteArray

	private external fun decrypt(
		passwordArray: ByteArray, passwordArrayLen: Int,
		cipherArray: ByteArray, cipherArrayLen: Int
	): ByteArray

}