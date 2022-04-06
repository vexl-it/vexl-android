package cz.cleevio.core.widget

import androidx.lifecycle.viewModelScope
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.marketplace.CryptoCurrencies
import cz.cleevio.repository.repository.marketplace.CryptoCurrencyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class CurrencyPriceChartViewModel constructor(
	private val cryptoCurrencyRepository: CryptoCurrencyRepository
) : BaseViewModel() {

	private val _currentCryptoCurrencyPrice = MutableSharedFlow<CryptoCurrencies>(replay = 1)
	val currentCryptoCurrencyPrice = _currentCryptoCurrencyPrice.asSharedFlow()

	init {
		loadCurrentPrice()
	}

	fun loadCurrentPrice() {
		viewModelScope.launch(Dispatchers.IO) {
			val response = cryptoCurrencyRepository.getCryptocurrencyPrice("bitcoin")
			when (response.status) {
				is Status.Success -> response.data?.let { data ->
					_currentCryptoCurrencyPrice.emit(data)
				}
			}
		}
	}

}