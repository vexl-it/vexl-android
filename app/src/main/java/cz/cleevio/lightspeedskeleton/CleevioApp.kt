package cz.cleevio.lightspeedskeleton

import android.app.Application
import com.cleevio.vexl.cryptography.EciesCryptoLib
import cz.cleevio.onboarding.di.onboardingModule
import cz.cleevio.cache.di.cacheModule
import cz.cleevio.core.di.coreModule
import cz.cleevio.core.utils.CustomDebugTree
import cz.cleevio.lightspeedskeleton.di.appModule
import cz.cleevio.lightspeedskeleton.di.viewModelsModule
import cz.cleevio.network.di.networkModule
import cz.cleevio.profile.di.profileModule
import cz.cleevio.repository.di.repoModule
import cz.cleevio.vexl.chat.di.chatModule
import cz.cleevio.vexl.contacts.di.contactsModule
import cz.cleevio.vexl.marketplace.di.marketplaceModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class CleevioApp : Application() {

	override fun onCreate() {
		super.onCreate()

		if (BuildConfig.DEBUG) {
			Timber.plant(CustomDebugTree())
		}

		EciesCryptoLib.init()

		startKoin {
			//androidLogger()
			androidContext(this@CleevioApp)
			modules(
				listOf(
					appModule,
					viewModelsModule,
					onboardingModule,
					contactsModule,
					marketplaceModule,
					coreModule,
					cacheModule,
					networkModule,
					repoModule,
					profileModule,
					chatModule
				)
			)
		}
	}
}