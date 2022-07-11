package cz.cleevio.network.api

import cz.cleevio.network.request.market.MarketChartRequest
import cz.cleevio.network.response.market.MarketChartResponse
import cz.cleevio.network.response.marketplace.CryptoCurrenciesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path

interface CryptocurrencyApi {

	@GET("cryptocurrencies/{coin}/")
	suspend fun getCryptocurrencyPrice(
		@Path(value = "coin") coin: String,
	): Response<CryptoCurrenciesResponse>

	@GET("cryptocurrencies/bitcoin/market_chart")
	suspend fun getMarketChartData(
		@Body bitcoinMarketDataRequest: MarketChartRequest
	): Response<MarketChartResponse>
}