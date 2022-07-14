package cz.cleevio.repository.repository.marketplace

import cz.cleevio.network.api.CryptocurrencyApi
import cz.cleevio.network.data.Resource
import cz.cleevio.network.extensions.tryOnline
import cz.cleevio.repository.model.marketplace.CryptoCurrencies
import cz.cleevio.repository.model.marketplace.MarketChartEntries
import cz.cleevio.repository.model.marketplace.fromNetwork

class CryptoCurrencyRepositoryImpl constructor(
	private val cryptocurrencyApi: CryptocurrencyApi
) : CryptoCurrencyRepository {

	override suspend fun getCryptocurrencyPrice(crypto: String): Resource<CryptoCurrencies> {
		return tryOnline(
			mapper = {
				it?.fromNetwork()
			},
			request = { cryptocurrencyApi.getCryptocurrencyPrice(crypto) }
		)
	}

	override suspend fun getMarketChartData(from: String, to: String, currency: String): Resource<MarketChartEntries> {
		return tryOnline(
			mapper = {
				MarketChartEntries(it?.prices ?: emptyList())
			},
			request = { cryptocurrencyApi.getMarketChartData(from, to, currency) }
		)
	}
}