package cz.cleeevio.vexl.marketplace.di

import cz.cleevio.core.widget.CurrencyPriceChartViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val marketplaceModule = module {

	viewModel {
		CurrencyPriceChartViewModel(
			cryptoCurrencyRepository = get()
		)
	}

}
