package cz.cleevio.network

import cz.cleevio.network.cache.NetworkCache
import cz.cleevio.network.data.ErrorIdentification
import cz.cleevio.network.response.TokenResponse
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator constructor(
	private val networkCache: NetworkCache,
	private val tokenRestApi: TokenRestApi
) : Authenticator {

	override fun authenticate(route: Route?, response: Response): Request? {
		synchronized(this) {
			if (responseCount(response) >= MAX_FAIL_COUNT) {
				// If we've failed 3 times, give up.
				handleUnauthorizedError()
				return null
			}

			if (networkCache.accessTokenGeneral.isNullOrEmpty() && networkCache.accessTokenGrant.isNullOrEmpty()) {
				// User is not logged (authorized) yet.
				return null
			}

			val tokenResponse = tokenRestApi.refreshToken(
				RestApi.createTokenHeader(
					networkCache.refreshToken.orEmpty()
				)
			).execute()

			if (tokenResponse.code() == ErrorIdentification.CODE_UNAUTHORIZED) {
				handleUnauthorizedError()
			} else if (tokenResponse.code() != ErrorIdentification.CODE_SUCCESS) {
				return null
			}

			val tokenBody = tokenResponse.body() ?: return null
			if (tokenBody.type == TokenResponse.TYPE_GRANT) {
				networkCache.accessTokenGrant = tokenBody.accessToken
			} else if (tokenBody.type == TokenResponse.TYPE_GENERAL) {
				networkCache.accessTokenGeneral = tokenBody.accessToken
			}
			networkCache.refreshToken = tokenBody.refreshToken

			return response.request.newBuilder()
				.header(RestApi.HEADER_AUTHORIZATION, RestApi.createTokenHeader(tokenBody.accessToken))
				.build()
		}
	}

	private fun responseCount(response: Response?): Int {
		var count = 1
		var r = response
		while (r?.priorResponse != null) {
			r = r.priorResponse
			count++
		}
		return count
	}

	private fun handleUnauthorizedError() {
		with(networkCache) {
			installUUID = null
			refreshToken = null
			accessTokenGrant = null
			accessTokenGeneral = null
		}
		// TODO remove user data
	}

	companion object {
		private const val MAX_FAIL_COUNT = 3
	}
}
