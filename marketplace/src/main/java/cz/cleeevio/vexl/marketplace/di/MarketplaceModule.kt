package cz.cleeevio.vexl.marketplace.di

import cz.cleeevio.vexl.marketplace.widgets.CurrencyPriceChartViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val marketplaceModule = module {

	viewModel {
		CurrencyPriceChartViewModel(
			cryptoCurrencyRepository = get()
		)
	}

}
