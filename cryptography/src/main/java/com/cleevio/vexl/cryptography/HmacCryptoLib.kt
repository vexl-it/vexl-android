package com.cleevio.vexl.cryptography

import okio.internal.commonToUtf8String
import java.nio.charset.StandardCharsets

object HmacCryptoLib : BaseCryptoLib() {

	fun digest(password: String, message: String): String {

		val passwordArray = password.toByteArray(StandardCharsets.UTF_8)
		val messageArray = message.toByteArray(StandardCharsets.UTF_8)

		val result = digest(
			passwordArray, passwordArray.size,
			messageArray, messageArray.size
		)

		return result.commonToUtf8String()
	}

	fun verify(password: String, message: String, digest: String): Boolean {

		val passwordArray = password.toByteArray(StandardCharsets.UTF_8)
		val messageArray = message.toByteArray(StandardCharsets.UTF_8)
		val digestArray = digest.toByteArray(StandardCharsets.UTF_8)

		return verify(
			passwordArray, passwordArray.size,
			messageArray, messageArray.size,
			digestArray, digestArray.size
		)
	}

	private external fun digest(
		passwordArray: ByteArray, passwordArrayLen: Int,
		messageArray: ByteArray, messageArrayLen: Int
	): ByteArray

	private external fun verify(
		passwordArray: ByteArray, passwordArrayLen: Int,
		messageArray: ByteArray, messageArrayLen: Int,
		digestArray: ByteArray, digestArrayLen: Int
	): Boolean

}