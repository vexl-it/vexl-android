package cz.cleevio.lightspeedskeleton.di

import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import cz.cleevio.lightspeedskeleton.BuildConfig
import cz.cleevio.lightspeedskeleton.R
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

const val DEBUG_FLAVOUR = "development"
private const val DEBUG_REMOTE_CONFIG_INTERVAL = 60L
private const val PRODUCTION_REMOTE_CONFIG_INTERVAL = 7200L
val appModule = module {

	single<TelephonyManager> {
		getSystemService(androidContext(), TelephonyManager::class.java) as TelephonyManager
	}

	single {
		val remoteConfig = Firebase.remoteConfig
		val configSettings = remoteConfigSettings {
			minimumFetchIntervalInSeconds =
				if (BuildConfig.FLAVOR == DEBUG_FLAVOUR) {
					DEBUG_REMOTE_CONFIG_INTERVAL
				} else {
					PRODUCTION_REMOTE_CONFIG_INTERVAL
				}
		}
		remoteConfig.setConfigSettingsAsync(configSettings)
		remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
		remoteConfig
	}
}
