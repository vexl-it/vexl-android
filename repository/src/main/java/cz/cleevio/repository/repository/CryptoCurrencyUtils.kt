package cz.cleevio.repository.repository

import cz.cleevio.repository.model.marketplace.CryptoCurrencies
import java.math.BigDecimal

class CryptoCurrencyUtils {

	private lateinit var cryptoCurrencies: CryptoCurrencies

	fun setCurrencies(currencies: CryptoCurrencies) {
		cryptoCurrencies = currencies
	}

	fun getPrice(currency: String): BigDecimal {
		return when (currency) {
			"CZK" -> {
				cryptoCurrencies.priceCzk
			}
			"USD" -> {
				cryptoCurrencies.priceUsd
			}
			"EUR" -> {
				cryptoCurrencies.priceEur
			}
			else -> {
				BigDecimal(0.0)
			}
		}
	}

}