package cz.cleevio.vexl.marketplace.di

import cz.cleevio.core.model.OfferType
import cz.cleevio.core.widget.CurrencyPriceChartViewModel
import cz.cleevio.vexl.marketplace.editOfferFragment.EditOfferViewModel
import cz.cleevio.vexl.marketplace.filtersFragment.FiltersViewModel
import cz.cleevio.vexl.marketplace.marketplaceFragment.MarketplaceViewModel
import cz.cleevio.vexl.marketplace.marketplaceFragment.offers.OffersViewModel
import cz.cleevio.vexl.marketplace.myOffersFragment.MyOffersViewModel
import cz.cleevio.vexl.marketplace.newOfferFragment.NewOfferViewModel
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
		)
	}

	viewModel {
		OffersViewModel(
			offerRepository = get()
		)
	}

	viewModel {
		NewOfferViewModel(
			userRepository = get(),
			offerRepository = get(),
			contactRepository = get(),
			chatRepository = get(),
			encryptedPreferenceRepository = get(),
			locationHelper = get()
		)
	}

	viewModel {
		RequestOfferViewModel(
			offerRepository = get(),
			chatRepository = get(),
			encryptedPreferenceRepository = get()
		)
	}

	viewModel {
		EditOfferViewModel(
			offerRepository = get(),
			contactRepository = get(),
			encryptedPreferenceRepository = get(),
			locationHelper = get(),
		)
	}

	viewModel { (offerType: OfferType) ->
		FiltersViewModel(
			offerType = offerType,
			offerRepository = get()
		)
	}

	viewModel { (offerType: OfferType) ->
		MyOffersViewModel(
			offerType = offerType,
			offerRepository = get(),
		)
	}
}
