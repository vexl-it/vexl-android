package cz.cleeevio.vexl.marketplace.di

import cz.cleeevio.vexl.marketplace.marketplaceFragment.MarketplaceViewModel
import cz.cleeevio.vexl.marketplace.marketplaceFragment.offers.OffersViewModel
import cz.cleeevio.vexl.marketplace.newOfferFragment.NewOfferViewModel
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
			offerRepository = get()
		)
	}

	viewModel {
		NewOfferViewModel(
			offerRepository = get(),
			contactRepository = get(),
			encryptedPreferenceRepository = get(),
			locationHelper = get()
		)
	}

}
