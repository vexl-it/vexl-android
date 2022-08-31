package cz.cleevio.repository.model.currency

import cz.cleevio.cache.entity.CryptoCurrencyEntity
import cz.cleevio.repository.model.Currency
import java.math.BigDecimal


class CryptoCurrencyValues constructor(
	val priceUsd: BigDecimal,
	val priceCzk: BigDecimal,
	val priceEur: BigDecimal
) {

	fun getPrice(currency: String): BigDecimal {
		return when (currency) {
			Currency.CZK.name -> {
				priceCzk
			}
			Currency.USD.name -> {
				priceUsd
			}
			Currency.EUR.name -> {
				priceEur
			}
			else -> {
				BigDecimal.ZERO
			}
		}
	}
}

fun CryptoCurrencyEntity.fromCache(): CryptoCurrencyValues = CryptoCurrencyValues(
	priceUsd = this.priceUsd,
	priceCzk = this.priceCzk,
	priceEur = this.priceEur
)