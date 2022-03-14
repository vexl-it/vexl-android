package cz.cleevio.core.di

import coil.ImageLoader
import coil.util.CoilUtils
import cz.cleevio.core.utils.NavMainGraphModel
import lightbase.camera.utils.ImageHelper
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {

	single {
		ImageLoader.Builder(androidContext())
			.crossfade(true)
			.okHttpClient {
				OkHttpClient.Builder()
					.cache(CoilUtils.createDefaultCache(androidContext()))
					.build()
			}
			.build()
	}

	single {
		ImageHelper()
	}

	single {
		NavMainGraphModel()
	}
}