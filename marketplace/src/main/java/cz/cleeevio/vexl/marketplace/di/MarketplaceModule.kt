package cz.cleeevio.vexl.marketplace.di

import cz.cleeevio.vexl.marketplace.marketplaceFragment.MarketplaceViewModel
import cz.cleeevio.vexl.marketplace.marketplaceFragment.offers.OffersViewModel
import cz.cleeevio.vexl.marketplace.newOfferFragment.NewOfferViewModel
import cz.cleeevio.vexl.marketplace.requestOfferFragment.RequestOfferViewModel
import cz.cleevio.core.widget.CurrencyPriceChartViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val marketplaceModule = module {

	viewModel {
		CurrencyPriceChartViewModel(
			cryptoCurrencyRepository = get()
		)
	}

	viewModel {
		MarketplaceViewModel(
			navMainGraphModel = get(),
			offerRepository = get()
		)
	}

	viewModel {
		OffersViewModel(
			offerRepository = get(),
			preferences = get()
		)
	}

	viewModel {
		NewOfferViewModel(
			offerRepository = get(),
			contactRepository = get(),
			chatRepository = get(),
			encryptedPreferenceRepository = get(),
			locationHelper = get()
		)
	}

	viewModel {
		RequestOfferViewModel(
			offerRepository = get()
		)
	}

}
