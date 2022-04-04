package cz.cleevio.repository.repository.marketplace

import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.marketplace.CryptoCurrencies

interface CryptoCurrencyRepository {

	suspend fun getCryptocurrencyPrice(crypto: String): Resource<CryptoCurrencies>

}