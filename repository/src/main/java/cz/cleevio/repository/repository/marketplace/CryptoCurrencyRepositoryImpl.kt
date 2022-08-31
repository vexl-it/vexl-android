package cz.cleevio.repository.repository.marketplace

import cz.cleevio.cache.dao.CryptoCurrencyDao
import cz.cleevio.cache.entity.CryptoCurrencyEntity
import cz.cleevio.network.api.CryptocurrencyApi
import cz.cleevio.network.data.Resource
import cz.cleevio.network.extensions.tryOnline
import cz.cleevio.repository.model.marketplace.CryptoCurrencies
import cz.cleevio.repository.model.marketplace.MarketChartEntries
import cz.cleevio.repository.model.marketplace.fromNetwork

const val CRYPTO_ENTITY_ID = 124875L

class CryptoCurrencyRepositoryImpl constructor(
	private val cryptocurrencyApi: CryptocurrencyApi,
	private val cryptoCurrencyDao: CryptoCurrencyDao
) : CryptoCurrencyRepository {

	override suspend fun getCryptocurrencyPrice(crypto: String): Resource<CryptoCurrencies> {
		return tryOnline(
			mapper = {
				it?.fromNetwork()
			},
			request = { cryptocurrencyApi.getCryptocurrencyPrice(crypto) },
			doOnSuccess = {
				it?.let { currencies ->
					cryptoCurrencyDao.replace(
						CryptoCurrencyEntity(
							id = CRYPTO_ENTITY_ID,
							priceUsd = currencies.priceUsd,
							priceCzk = currencies.priceCzk,
							priceEur = currencies.priceEur,
						)
					)
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