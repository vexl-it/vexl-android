package cz.cleevio.lightspeedskeleton.di

import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat.getSystemService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {

	single<TelephonyManager> {
		getSystemService(androidContext(), TelephonyManager::class.java) as TelephonyManager
	}
}
