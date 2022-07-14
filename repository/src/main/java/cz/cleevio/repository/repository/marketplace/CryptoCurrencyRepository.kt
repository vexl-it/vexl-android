package cz.cleevio.repository.repository.marketplace

import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.marketplace.CryptoCurrencies
import cz.cleevio.repository.model.marketplace.MarketChartEntries

interface CryptoCurrencyRepository {

	suspend fun getCryptocurrencyPrice(crypto: String): Resource<CryptoCurrencies>

	suspend fun getMarketChartData(from: String, to: String, currency: String): Resource<MarketChartEntries>
}