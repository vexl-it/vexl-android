package cz.cleeevio.vexl.marketplace.di

import cz.cleeevio.vexl.marketplace.editOfferFragment.EditOfferViewModel
import cz.cleeevio.vexl.marketplace.marketplaceFragment.MarketplaceViewModel
import cz.cleeevio.vexl.marketplace.marketplaceFragment.offers.OffersViewModel
import cz.cleeevio.vexl.marketplace.myOffersFragment.MyOffersViewModel
import cz.cleeevio.vexl.marketplace.newOfferFragment.NewOfferViewModel
import cz.cleeevio.vexl.marketplace.requestOfferFragment.RequestOfferViewModel
import cz.cleevio.core.model.OfferType
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
			offerRepository = get(),
			userRepository = get(),
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
		MyOffersViewModel(
			offerType = offerType,
			offerRepository = get(),
		)
	}
}
