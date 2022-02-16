package cz.cleevio.pin.di

import lightbase.pin.LBPinViewModel
import lightbase.security.FingerprintBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val pinModule = module {

	single {
		LBPinViewModel(
			context = androidContext()
		)
	}

	single {
		FingerprintBuilder()
	}
}