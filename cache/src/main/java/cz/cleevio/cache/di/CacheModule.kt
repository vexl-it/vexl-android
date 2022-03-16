package cz.cleevio.cache.di

import androidx.room.Room
import cz.cleevio.cache.CleevioDatabase
import cz.cleevio.cache.preferences.DataStoreRepository
import cz.cleevio.cache.preferences.DataStoreRepositoryImpl
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.cache.preferences.EncryptedPreferenceRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val cacheModule = module {
	single {
		Room.databaseBuilder(
			androidContext(),
			CleevioDatabase::class.java,
			"CleevioDatabase.db"
		)
			.build()
	}

	single {
		get<CleevioDatabase>().userDao()
	}

	single {
		get<CleevioDatabase>().contactDao()
	}

	single<DataStoreRepository> {
		DataStoreRepositoryImpl(
			context = androidContext()
		)
	}

	single<EncryptedPreferenceRepository> {
		EncryptedPreferenceRepositoryImpl(
			context = androidContext()
		)
	}
}
