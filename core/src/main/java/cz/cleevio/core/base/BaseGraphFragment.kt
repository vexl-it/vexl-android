package cz.cleevio.core.base

import androidx.annotation.CallSuper
import cz.cleevio.core.model.CryptoCurrency.Companion.mapStringToCryptoCurrency
import cz.cleevio.core.model.Currency.Companion.mapStringToCurrency
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.widget.CurrencyPriceChartViewModel
import cz.cleevio.core.widget.CurrencyPriceChartWidget
import lightbase.core.baseClasses.BaseFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

abstract class BaseGraphFragment(fragment: Int) : BaseFragment(fragment) {

	override val viewModel by sharedViewModel<CurrencyPriceChartViewModel>()

	// Always initialize chart widget in implementing Fragment
	abstract var priceChartWidget: CurrencyPriceChartWidget?

	@CallSuper
	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.currentCryptoCurrencyPrice.collect { currentCryptoCurrencyPrice ->
				priceChartWidget?.setupCryptoCurrencies(currentCryptoCurrencyPrice)
				priceChartWidget?.setupCurrencies(
					viewModel.encryptedPreferenceRepository.selectedCurrency.mapStringToCurrency(),
					viewModel.encryptedPreferenceRepository.selectedCryptoCurrency.mapStringToCryptoCurrency()
				)
			}
		}
		repeatScopeOnStart {
			viewModel.marketData.collect { marketData ->
				priceChartWidget?.setupMarketData(marketData)
				priceChartWidget?.setupTimeRange(viewModel.dateTimeRange)
			}
		}
	}

	@CallSuper
	override fun onResume() {
		super.onResume()
		viewModel.syncMarketData()
	}

	@CallSuper
	override fun initView() {
		priceChartWidget?.onPriceChartPeriodClicked = {
			viewModel.syncMarketData(it)
		}
	}
}