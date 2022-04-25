package cz.cleevio.repository.di

import cz.cleevio.repository.PhoneNumberUtils
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.contact.ContactRepositoryImpl
import cz.cleevio.repository.repository.marketplace.CryptoCurrencyRepository
import cz.cleevio.repository.repository.marketplace.CryptoCurrencyRepositoryImpl
import cz.cleevio.repository.repository.offer.OfferRepository
import cz.cleevio.repository.repository.offer.OfferRepositoryImpl
import cz.cleevio.repository.repository.user.UserRepository
import cz.cleevio.repository.repository.user.UserRepositoryImpl
import org.koin.dsl.module

val repoModule = module {

	single<UserRepository> {
		UserRepositoryImpl(
			userDao = get(),
			encryptedPreference = get(),
			userRestApi = get()
		)
	}

	single<OfferRepository> {
		OfferRepositoryImpl(
			offerApi = get()
		)
	}

	single<ContactRepository> {
		ContactRepositoryImpl(
			contactDao = get(),
			contactKeyDao = get(),
			contactApi = get(),
			phoneNumberUtils = get(),
			encryptedPreference = get()
		)
	}

	single<CryptoCurrencyRepository> {
		CryptoCurrencyRepositoryImpl(
			cryptocurrencyApi = get()
		)
	}

	single {
		PhoneNumberUtils(
			telephonyManager = get()
		)
	}
}