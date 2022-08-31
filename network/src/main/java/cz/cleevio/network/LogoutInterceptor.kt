package cz.cleevio.network

import cz.cleevio.network.data.ErrorIdentification.Companion.CODE_FORBIDDEN_403
import okhttp3.Interceptor
import okhttp3.Response

class LogoutInterceptor constructor(
	val networkError: NetworkError
) : Interceptor {

	override fun intercept(chain: Interceptor.Chain): Response {
		val response = chain.proceed(chain.request())
		if (response.code == CODE_FORBIDDEN_403) {
			networkError.sendLogout()
		}
		return response
	}
}