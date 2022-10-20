package cz.cleevio.core.utils

import android.util.Base64
import java.security.SecureRandom

const val BYTE_ARRAY_SIZE = 256

class EncryptionUtils {
	val random = SecureRandom()

	fun generateAesSymmetricalKey(): String {
		val bytes = ByteArray(BYTE_ARRAY_SIZE)
		random.nextBytes(bytes)
		return Base64.encodeToString(bytes, Base64.DEFAULT or Base64.NO_WRAP)
	}
}