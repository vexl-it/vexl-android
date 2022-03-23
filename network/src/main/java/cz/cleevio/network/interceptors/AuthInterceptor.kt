package cz.cleevio.network.interceptors

import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor constructor(
	private val encryptedPreference: EncryptedPreferenceRepository
) : Interceptor {

	override fun intercept(chain: Interceptor.Chain): Response {
		val request = chain
			.request()
		val requestBuilder = request
			.newBuilder()

		val signature = encryptedPreference.signature
		if (request.header(HEADER_SIGNATURE).isNullOrEmpty() && signature.isNotEmpty()) {
			requestBuilder.header(HEADER_SIGNATURE, signature)
		}

		val hash = encryptedPreference.hash
		if (request.header(HEADER_HASH).isNullOrEmpty() && hash.isNotEmpty()) {
			requestBuilder.header(HEADER_HASH, hash)
		}

		val userPublicKey = encryptedPreference.userPublicKey
		if (request.header(HEADER_PUBLIC_KEY).isNullOrEmpty() && userPublicKey.isNotEmpty()) {
			requestBuilder.header(HEADER_PUBLIC_KEY, userPublicKey)
		}

		return chain.proceed(requestBuilder.build())
	}

	companion object {
		const val HEADER_SIGNATURE = "signature"
		const val HEADER_HASH = "hash"
		const val HEADER_PUBLIC_KEY = "public-key"
	}
}