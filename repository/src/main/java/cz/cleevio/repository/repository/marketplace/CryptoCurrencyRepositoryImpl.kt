package cz.cleevio.repository.repository.marketplace

import cz.cleevio.network.api.CryptocurrencyApi
import cz.cleevio.network.data.Resource
import cz.cleevio.network.extensions.tryOnline
import cz.cleevio.repository.model.marketplace.CryptoCurrencies
import cz.cleevio.repository.model.marketplace.MarketChartEntries
import cz.cleevio.repository.model.marketplace.fromNetwork
import cz.cleevio.repository.repository.CryptoCurrencyUtils

class CryptoCurrencyRepositoryImpl constructor(
	private val cryptocurrencyApi: CryptocurrencyApi,
	private val cryptoCurrencyUtils: CryptoCurrencyUtils
) : CryptoCurrencyRepository {

	override suspend fun getCryptocurrencyPrice(crypto: String): Resource<CryptoCurrencies> {
		return tryOnline(
			mapper = {
				it?.fromNetwork()
			},
			request = { cryptocurrencyApi.getCryptocurrencyPrice(crypto) },
			doOnSuccess = {
				it?.let { currencies ->
					cryptoCurrencyUtils.setCurrencies(currencies)
				}
			}
		)
	}

	override suspend fun getMarketChartData(duration: String, currency: String): Resource<MarketChartEntries> {
		return tryOnline(
			mapper = {
				MarketChartEntries(it?.prices ?: emptyList())
			},
			request = { cryptocurrencyApi.getMarketChartData(duration, currency) }
		)
	}
}