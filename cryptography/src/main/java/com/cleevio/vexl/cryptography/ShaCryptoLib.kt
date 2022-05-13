package com.cleevio.vexl.cryptography

import okio.internal.commonToUtf8String
import java.nio.charset.StandardCharsets

object ShaCryptoLib : BaseCryptoLib() {

	fun hash(data: String): String {
		val dataArray = data.toByteArray(StandardCharsets.UTF_8)
		val result = hash(dataArray, dataArray.size)
		return result.commonToUtf8String()
	}

	private external fun hash(dataArray: ByteArray, dataArrayLen: Int): ByteArray

}