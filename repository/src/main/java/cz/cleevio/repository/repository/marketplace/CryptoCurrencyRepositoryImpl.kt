package cz.cleevio.repository.repository.marketplace

import cz.cleevio.network.api.CryptocurrencyApi
import cz.cleevio.network.data.Resource
import cz.cleevio.network.extensions.tryOnline
import cz.cleevio.repository.model.marketplace.CryptoCurrencies
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

}