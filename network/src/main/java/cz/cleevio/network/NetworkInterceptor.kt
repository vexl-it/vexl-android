package cz.cleevio.network

import cz.cleevio.network.agent.UserAgent
import cz.cleevio.network.cache.NetworkCache
import cz.cleevio.network.language.LanguageTracker
import okhttp3.Interceptor
import okhttp3.Response

class NetworkInterceptor constructor(
	private val networkCache: NetworkCache,
	private val languageTracker: LanguageTracker,
	private val userAgent: UserAgent
) : Interceptor {

	override fun intercept(chain: Interceptor.Chain): Response {
		val request = chain
			.request()
			.newBuilder()
			.addHeader(HEADER_CONTENT_TYPE_KEY, HEADER_CONTENT_TYPE_VALUE)
			.addHeader(HEADER_X_PLATFORM, HEADER_X_PLATFORM_VALUE)
			.addHeader(HEADER_X_INSTALL_UUID, networkCache.installUUID.orEmpty())
			.addHeader(HEADER_ACCEPT_LANGUAGE, languageTracker.getAppLanguage())
			.addHeader(HEADER_USER_AGENT, userAgent.getApplicationIdentifier())
		val newRequest = request.build()
		return chain.proceed(newRequest)
	}

	/**
	 * Simulates time-expensive operation.
	 */
	@Suppress("unused")
	private fun expensiveOperation() {
		try {
			Thread.sleep(EXPENSIVE_OPERATION_DELAY)
		} catch (e: InterruptedException) {
			// this is ok
		}
	}

	companion object {
		const val HEADER_CONTENT_TYPE_KEY = "Content-Type"
		const val HEADER_CONTENT_TYPE_VALUE = "application/json; charset=utf-8"
		const val HEADER_X_PLATFORM = "X-Platform"
		const val HEADER_X_INSTALL_UUID = "X-Install-UUID"
		const val HEADER_X_PLATFORM_VALUE = "ANDROID"
		const val HEADER_ACCEPT_LANGUAGE = "Accept-Language"
		const val HEADER_USER_AGENT = "User-Agent"

		private const val EXPENSIVE_OPERATION_DELAY = 2000L
	}
}
