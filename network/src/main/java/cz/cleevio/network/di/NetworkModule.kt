package cz.cleevio.network.di

import com.squareup.moshi.Moshi
import cz.cleevio.network.*
import cz.cleevio.network.agent.UserAgent
import cz.cleevio.network.agent.UserAgentImpl
import cz.cleevio.network.cache.NetworkCache
import cz.cleevio.network.cache.NetworkCacheImpl
import cz.cleevio.network.language.LanguageTracker
import cz.cleevio.network.language.LanguageTrackerImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber

val networkModule = module {

	single {
		get<Retrofit>().create(RestApi::class.java)
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
		val httpClient = OkHttpClient.Builder()
			.addInterceptor(get<NetworkInterceptor>())
			.apply {
				if (BuildConfig.DEBUG) {
					addInterceptor(get<HttpLoggingInterceptor>())
				}
			}
			.build()

		val retrofit = Retrofit.Builder()
			.baseUrl(BuildConfig.API_BASE_URL)
			.client(httpClient)
			.addConverterFactory(EmptyBodyConverterFactory)
			.addConverterFactory(UnitConverterFactory)
			.addConverterFactory(MoshiConverterFactory.create(get()).withNullSerialization())
			.build()

		retrofit.create(TokenRestApi::class.java)
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
		Moshi.Builder().build()
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

	single {
		NetworkInterceptor(
			networkCache = get(),
			languageTracker = get(),
			userAgent = get()
		)
	}

	single {
		TokenAuthenticator(
			networkCache = get(),
			tokenRestApi = get()
		)
	}
}
