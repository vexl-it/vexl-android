package cz.cleevio.network.interceptors

import cz.cleevio.network.cache.NetworkCache
import okhttp3.Interceptor
import okhttp3.Response


class AuthInterceptor constructor(
	private val networkCache: NetworkCache,
) : Interceptor {

	override fun intercept(chain: Interceptor.Chain): Response {
		val request = chain
			.request()
		val requestBuilder = request
			.newBuilder()

		val token = networkCache.accessTokenGeneral
		//if we have accessToken and newRequest doesn't have Auth header already set
		if (request.header(HEADER_AUTHORIZATION).isNullOrEmpty() && token?.isNotEmpty() == true) {
			requestBuilder.header(HEADER_AUTHORIZATION, createTokenHeader(token))
		}

		return chain.proceed(requestBuilder.build())
	}

	companion object {
		const val HEADER_AUTHORIZATION = "Authorization"
		private const val HEADER_AUTHORIZATION_BEARER = "Bearer %s"

		fun createTokenHeader(token: String?): String {
			return String.format(HEADER_AUTHORIZATION_BEARER, token.orEmpty())
		}

		fun createTokenHeaderOptional(token: String?): String? {
			return if (token.isNullOrEmpty())
				null
			else
				String.format(HEADER_AUTHORIZATION_BEARER, token.orEmpty())
		}
	}
}