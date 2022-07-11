package cz.cleevio.repository.repository.marketplace

import cz.cleevio.network.data.Resource
import cz.cleevio.network.request.market.MarketChartRequest
import cz.cleevio.repository.model.marketplace.CryptoCurrencies
import cz.cleevio.repository.model.marketplace.MarketChartEntries

interface CryptoCurrencyRepository {

	suspend fun getCryptocurrencyPrice(crypto: String): Resource<CryptoCurrencies>

	suspend fun getMarketChartData(marketChartRequest: MarketChartRequest): Resource<MarketChartEntries>
}