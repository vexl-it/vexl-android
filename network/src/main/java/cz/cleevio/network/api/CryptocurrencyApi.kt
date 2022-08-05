package cz.cleevio.network.api

import cz.cleevio.network.response.market.MarketChartResponse
import cz.cleevio.network.response.marketplace.CryptoCurrenciesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CryptocurrencyApi {

	@GET("cryptocurrencies/{coin}/")
	suspend fun getCryptocurrencyPrice(
		@Path(value = "coin") coin: String,
	): Response<CryptoCurrenciesResponse>

	@GET("cryptocurrencies/bitcoin/market_chart")
	suspend fun getMarketChartData(
		@Query("duration") duration: String,
		@Query("currency") currency: String
	): Response<MarketChartResponse>
}