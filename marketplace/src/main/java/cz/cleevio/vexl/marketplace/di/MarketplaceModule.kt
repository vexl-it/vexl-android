package cz.cleevio.vexl.marketplace.di

import cz.cleevio.core.model.OfferEncryptionData
import cz.cleevio.core.model.OfferType
import cz.cleevio.core.widget.CurrencyPriceChartViewModel
import cz.cleevio.vexl.marketplace.editOfferFragment.EditOfferViewModel
import cz.cleevio.vexl.marketplace.encryptingProgressFragment.EncryptingProgressViewModel
import cz.cleevio.vexl.marketplace.filtersFragment.FiltersViewModel
import cz.cleevio.vexl.marketplace.marketplaceFragment.MarketplaceViewModel
import cz.cleevio.vexl.marketplace.marketplaceFragment.NavMarketplaceGraphModel
import cz.cleevio.vexl.marketplace.marketplaceFragment.offers.OffersViewModel
import cz.cleevio.vexl.marketplace.myOffersFragment.MyOffersViewModel
import cz.cleevio.vexl.marketplace.newOfferFragment.NewOfferViewModel
import cz.cleevio.vexl.marketplace.reportOffer.ReportOfferConfirmViewModel
import cz.cleevio.vexl.marketplace.reportOffer.ReportOfferViewModel
import cz.cleevio.vexl.marketplace.requestOfferFragment.RequestOfferViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val marketplaceModule = module {

	viewModel {
		CurrencyPriceChartViewModel(
			cryptoCurrencyRepository = get(),
			encryptedPreferenceRepository = get()
		)
	}

	viewModel {
		MarketplaceViewModel(
			navMainGraphModel = get(),
			offerRepository = get(),
			userRepository = get(),
			chatRepository = get(),
			groupRepository = get(),
			encryptedPreference = get(),
			navMarketplaceGraphModel = get()
		)
	}

	viewModel {
		OffersViewModel(
			groupRepository = get(),
			offerRepository = get(),
			navMarketplaceGraphModel = get(),
			remoteConfig = get(),
		)
	}

	viewModel {
		NewOfferViewModel(
			userRepository = get(),
			offerRepository = get(),
			contactRepository = get(),
			groupRepository = get(),
			encryptedPreferenceRepository = get(),
			locationHelper = get()
		)
	}

	viewModel { (offerEncryptionData: OfferEncryptionData) ->
		EncryptingProgressViewModel(
			offerEncryptionData = offerEncryptionData,
			encryptedPreferenceRepository = get(),
			offerRepository = get()
		)
	}

	viewModel {
		RequestOfferViewModel(
			groupRepository = get(),
			offerRepository = get(),
			chatRepository = get(),
			encryptedPreferenceRepository = get()
		)
	}

	viewModel {
		EditOfferViewModel(
			userRepository = get(),
			offerRepository = get(),
			contactRepository = get(),
			encryptedPreferenceRepository = get(),
			locationHelper = get(),
			groupRepository = get(),
		)
	}

	viewModel { (offerType: OfferType) ->
		FiltersViewModel(
			offerType = offerType,
			offerRepository = get(),
			groupRepository = get(),
			userRepository = get()
		)
	}

	viewModel { (offerType: OfferType) ->
		MyOffersViewModel(
			offerType = offerType,
			offerRepository = get(),
			groupRepository = get(),
		)
	}

	viewModel { (offerId: String) ->
		ReportOfferViewModel(
			offerId = offerId,
			offerRepository = get()
		)
	}


	viewModel {
		ReportOfferConfirmViewModel(
			navMainGraphModel = get()
		)
	}

	single {
		NavMarketplaceGraphModel()
	}
}
