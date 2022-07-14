package cz.cleevio.core.widget

import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.model.CryptoCurrency
import cz.cleevio.core.model.CryptoCurrency.Companion.getExactName
import cz.cleevio.core.model.Currency
import cz.cleevio.core.model.MarketChartEntry
import cz.cleevio.core.utils.DateTimeRange
import cz.cleevio.core.utils.getChartTimeRange
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.marketplace.CryptoCurrencies
import cz.cleevio.repository.repository.marketplace.CryptoCurrencyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class CurrencyPriceChartViewModel constructor(
	private val cryptoCurrencyRepository: CryptoCurrencyRepository,
	val encryptedPreferenceRepository: EncryptedPreferenceRepository
) : BaseViewModel() {

	private val _currentCryptoCurrencyPrice = MutableSharedFlow<CryptoCurrencies>(replay = 1)
	val currentCryptoCurrencyPrice = _currentCryptoCurrencyPrice.asSharedFlow()

	private val _marketData = MutableSharedFlow<MarketChartEntry>(replay = 1)
	val marketData = _marketData.asSharedFlow()

	private var dateTimeRange: DateTimeRange? = null

	init {
		// TODO move into currency selector in onboarding
		encryptedPreferenceRepository.selectedCurrency = Currency.USD.name
		encryptedPreferenceRepository.selectedCryptoCurrency = CryptoCurrency.BITCOIN.getExactName()
	}

	fun syncMarketData(time: DateTimeRange? = null) {
		viewModelScope.launch(Dispatchers.IO) {
			loadCurrentPrice()
			getMarketRates(time ?: dateTimeRange ?: DEFAULT_TIME_RANGE)
		}
	}

	private fun getMarketRates(timeRange: DateTimeRange) {
		viewModelScope.launch(Dispatchers.IO) {
			dateTimeRange = timeRange

			val chartTimeRange = getChartTimeRange(timeRange)
			val response = cryptoCurrencyRepository.getMarketChartData(
				from = chartTimeRange.first,
				to = chartTimeRange.second,
				currency = encryptedPreferenceRepository.selectedCurrency
			)
			response.data?.let { marketRates ->
				var x = -1f
				val entries = marketRates.entries.map { entries ->
					x++
					Entry(
						entries[0].toFloat(),
						entries[1].toFloat()
					)
				}.toMutableList()

				// If there is only one entry than double it for correct view
				if (entries.size == 1) {
					entries.firstOrNull { entry ->
						val newEntry = entry.copy()
						newEntry.x++
						entries.add(newEntry)
					}
				}

				val minEntry = entries.minByOrNull { it.y }
				val maxEntry = entries.maxByOrNull { it.y }

				_marketData.emit(
					MarketChartEntry(
						minEntry = minEntry,
						maxEntry = maxEntry,
						entries = entries
					)
				)
			}
		}
	}

	private fun loadCurrentPrice() {
		viewModelScope.launch(Dispatchers.IO) {
			val response =
				cryptoCurrencyRepository.getCryptocurrencyPrice(
					crypto = encryptedPreferenceRepository.selectedCryptoCurrency
				)
			when (response.status) {
				is Status.Success -> response.data?.let { data ->
					_currentCryptoCurrencyPrice.emit(data)
				}
				else -> {
					// Do nothing
				}
			}
		}
	}

	companion object {
		private val DEFAULT_TIME_RANGE = DateTimeRange.DAY
	}
}