package cz.cleevio.repository.di

import cz.cleevio.repository.PhoneNumberUtils
import cz.cleevio.repository.repository.chat.ChatRepository
import cz.cleevio.repository.repository.chat.ChatRepositoryImpl
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.contact.ContactRepositoryImpl
import cz.cleevio.repository.repository.group.GroupRepository
import cz.cleevio.repository.repository.group.GroupRepositoryImpl
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
			offerApi = get(),
			myOfferDao = get(),
			offerDao = get(),
			locationDao = get(),
			transactionProvider = get(),
			requestedOfferDao = get(),
			contactDao = get(),
			offerCommonFriendCrossRefDao = get(),
			chatRepository = get(),
		)
	}

	single<ContactRepository> {
		ContactRepositoryImpl(
			contactDao = get(),
			contactKeyDao = get(),
			contactApi = get(),
			phoneNumberUtils = get(),
			encryptedPreference = get(),
			notificationDao = get()
		)
	}

	single<CryptoCurrencyRepository> {
		CryptoCurrencyRepositoryImpl(
			cryptocurrencyApi = get()
		)
	}

	single<ChatRepository> {
		ChatRepositoryImpl(
			chatApi = get(),
			contactApi = get(),
			notificationDao = get(),
			chatMessageDao = get(),
			myOfferDao = get(),
			requestedOfferDao = get(),
			offerDao = get(),
			encryptedPreferenceRepository = get(),
			chatUserDao = get()
		)
	}

	single<GroupRepository> {
		GroupRepositoryImpl(
			groupApi = get(),
			groupDao = get(),
			contactKeyDao = get(),
			myOfferDao = get()
		)
	}

	single {
		PhoneNumberUtils(
			telephonyManager = get()
		)
	}
}