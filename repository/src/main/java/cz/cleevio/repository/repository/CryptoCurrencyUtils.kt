package cz.cleevio.repository.repository

import cz.cleevio.repository.model.Currency
import cz.cleevio.repository.model.marketplace.CryptoCurrencies
import java.math.BigDecimal

class CryptoCurrencyUtils {

	private var cryptoCurrencies: CryptoCurrencies? = null

	fun setCurrencies(currencies: CryptoCurrencies?) {
		cryptoCurrencies = currencies
	}

	fun getPrice(currency: String): BigDecimal {
		return when (currency) {
			Currency.CZK.name -> {
				cryptoCurrencies?.priceCzk ?: BigDecimal.ZERO
			}
			Currency.USD.name -> {
				cryptoCurrencies?.priceUsd ?: BigDecimal.ZERO
			}
			Currency.EUR.name -> {
				cryptoCurrencies?.priceEur ?: BigDecimal.ZERO
			}
			else -> {
				BigDecimal.ZERO
			}
		}
	}

}