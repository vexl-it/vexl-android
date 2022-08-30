package cz.cleevio.repository.repository

import cz.cleevio.repository.model.Currency
import cz.cleevio.repository.model.marketplace.CryptoCurrencies
import java.math.BigDecimal

class CryptoCurrencyUtils {

	private lateinit var cryptoCurrencies: CryptoCurrencies

	fun setCurrencies(currencies: CryptoCurrencies) {
		cryptoCurrencies = currencies
	}

	fun getPrice(currency: String): BigDecimal {
		return when (currency) {
			Currency.CZK.name -> {
				cryptoCurrencies.priceCzk
			}
			Currency.USD.name -> {
				cryptoCurrencies.priceUsd
			}
			Currency.EUR.name -> {
				cryptoCurrencies.priceEur
			}
			else -> {
				BigDecimal(0.0)
			}
		}
	}

}