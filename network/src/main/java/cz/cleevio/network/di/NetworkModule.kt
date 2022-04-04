package cz.cleevio.network.di

import com.squareup.moshi.Moshi
import cz.cleevio.network.*
import cz.cleevio.network.adapters.BigDecimalAdapter
import cz.cleevio.network.agent.UserAgent
import cz.cleevio.network.agent.UserAgentImpl
import cz.cleevio.network.api.ContactApi
import cz.cleevio.network.api.OfferApi
import cz.cleevio.network.api.CryptocurrencyApi
import cz.cleevio.network.api.UserApi
import cz.cleevio.network.cache.NetworkCache
import cz.cleevio.network.cache.NetworkCacheImpl
import cz.cleevio.network.interceptors.AuthInterceptor
import cz.cleevio.network.language.LanguageTracker
import cz.cleevio.network.language.LanguageTrackerImpl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

const val NETWORK_INTERCEPTOR = "NETWORK_INTERCEPTOR"
const val AUTH_INTERCEPTOR = "AUTH_INTERCEPTOR"
const val HTTP_LOGGING_INTERCEPTOR = "HTTP_LOGGING_INTERCEPTOR"
const val NETWORK_REQUEST_TIMEOUT = 30L

const val USER_API_BASE_URL = "https://user.vexl.devel.cleevio.io/api/v1/"
const val CONTACT_API_BASE_URL = "https://contact.vexl.devel.cleevio.io/api/v1/"
const val OFFER_API_BASE_URL = "https://offer.vexl.devel.cleevio.io/api/v1/"

val networkModule = module {

	fun provideRetrofit(
		baseUrl: String,
		scope: Scope,
		interceptors: List<Interceptor>,
		tokenAuthenticator: TokenAuthenticator?
	): Retrofit {
		val builder = OkHttpClient.Builder()
		interceptors.forEach {
			builder.addInterceptor(it)
		}
		tokenAuthenticator?.let {
			builder.authenticator(it)
		}
		builder.connectTimeout(NETWORK_REQUEST_TIMEOUT, TimeUnit.SECONDS)
		builder.readTimeout(NETWORK_REQUEST_TIMEOUT, TimeUnit.SECONDS)
		builder.writeTimeout(NETWORK_REQUEST_TIMEOUT, TimeUnit.SECONDS)
		val client = builder.build()

		return Retrofit.Builder()
			.baseUrl(baseUrl)
			.client(client)
			.addConverterFactory(EmptyBodyConverterFactory)
			.addConverterFactory(UnitConverterFactory)
			.addConverterFactory(MoshiConverterFactory.create(scope.get()).withNullSerialization())
			.build()
	}

	single {
		get<Retrofit>().create(RestApi::class.java)
	}

	//should be removed OR changed to use User microservice
	single {
		provideRetrofit(
			scope = this,
			interceptors = listOf(
				get(named(NETWORK_INTERCEPTOR)),
				get(named(HTTP_LOGGING_INTERCEPTOR))
			),
			tokenAuthenticator = null,
			baseUrl = BuildConfig.API_BASE_URL
		).create(TokenRestApi::class.java)
	}

	single {
		provideRetrofit(
			scope = this,
			interceptors = listOf(
				get(named(AUTH_INTERCEPTOR)),
				get(named(NETWORK_INTERCEPTOR)),
				get(named(HTTP_LOGGING_INTERCEPTOR))
			),
			tokenAuthenticator = get(),
			baseUrl = USER_API_BASE_URL
		).create(UserApi::class.java)
	}

	single {
		provideRetrofit(
			scope = this,
			interceptors = listOf(
				get(named(AUTH_INTERCEPTOR)),
				get(named(NETWORK_INTERCEPTOR)),
				get(named(HTTP_LOGGING_INTERCEPTOR))
			),
			tokenAuthenticator = get(),
			baseUrl = CONTACT_API_BASE_URL
		).create(ContactApi::class.java)
	}

	single {
		provideRetrofit(
			scope = this,
			interceptors = listOf(
				get(named(AUTH_INTERCEPTOR)),
				get(named(NETWORK_INTERCEPTOR)),
				get(named(HTTP_LOGGING_INTERCEPTOR))
			),
			tokenAuthenticator = get(),
			baseUrl = OFFER_API_BASE_URL
		).create(OfferApi::class.java)
	}

	single {
		provideRetrofit(
			scope = this,
			interceptors = listOf(
				get(named(AUTH_INTERCEPTOR)),
				get(named(NETWORK_INTERCEPTOR)),
				get(named(HTTP_LOGGING_INTERCEPTOR))
			),
			tokenAuthenticator = get(),
			baseUrl = USER_API_BASE_URL
		).create(CryptocurrencyApi::class.java)
	}

	single {
		OkHttpClient.Builder()
			.addInterceptor(get<NetworkInterceptor>())
			.authenticator(get<TokenAuthenticator>())
			.apply {
				if (BuildConfig.DEBUG) {
					addInterceptor(get<HttpLoggingInterceptor>())
				}
			}
			.build()
	}

	single {
		Retrofit.Builder()
			.baseUrl(BuildConfig.API_BASE_URL)
			.client(get())
			.addConverterFactory(EmptyBodyConverterFactory)
			.addConverterFactory(UnitConverterFactory)
			.addConverterFactory(MoshiConverterFactory.create(get()).withNullSerialization())
			.build()
	}

	single {
		val logging = HttpLoggingInterceptor.Logger { message ->
			Timber.tag("OkHttp").d(message)
		}
		HttpLoggingInterceptor(logging).apply {
			level = HttpLoggingInterceptor.Level.BODY
		}
	}

	single {
		Moshi
			.Builder()
			.add(BigDecimalAdapter)
			.build()
	}

	single<LanguageTracker> {
		LanguageTrackerImpl(
			appContext = androidContext()
		)
	}

	single<NetworkCache> {
		NetworkCacheImpl(
			appContext = androidContext()
		)
	}

	single<UserAgent> {
		UserAgentImpl(
			appContext = androidContext()
		)
	}

	single(named(AUTH_INTERCEPTOR)) {
		AuthInterceptor(
			encryptedPreference = get()
		)
	} bind Interceptor::class

	single(named(NETWORK_INTERCEPTOR)) {
		NetworkInterceptor(
			networkCache = get(),
			languageTracker = get(),
			userAgent = get()
		)
	} bind Interceptor::class

	single(named(HTTP_LOGGING_INTERCEPTOR)) {
		val logging = HttpLoggingInterceptor.Logger { message ->
			Timber.tag("OkHttp").d(message)
		}
		HttpLoggingInterceptor(logging).apply {
			level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
		}
	} bind Interceptor::class

	single {
		TokenAuthenticator(
			networkCache = get(),
			tokenRestApi = get()
		)
	}

	single {
		NetworkError()
	}
}
