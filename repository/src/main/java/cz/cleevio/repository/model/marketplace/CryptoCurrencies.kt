package cz.cleevio.repository.model.marketplace

import cz.cleevio.network.response.marketplace.CryptoCurrenciesResponse
import java.math.BigDecimal
import java.time.ZonedDateTime

data class CryptoCurrencies(
	val priceUsd: BigDecimal,
	val priceChangePercentage24h: BigDecimal,
	val priceChangePercentage7d: BigDecimal,
	val priceChangePercentage14d: BigDecimal,
	val priceChangePercentage30d: BigDecimal,
	val priceChangePercentage60d: BigDecimal,
	val priceChangePercentage200d: BigDecimal,
	val priceChangePercentage1y: BigDecimal,
	val lastUpdated: ZonedDateTime
)

fun CryptoCurrenciesResponse.fromNetwork(): CryptoCurrencies {
	return CryptoCurrencies(
		priceUsd = this.priceUsd,
		priceChangePercentage24h = this.priceChangePercentage24h,
		priceChangePercentage7d = this.priceChangePercentage7d,
		priceChangePercentage14d = this.priceChangePercentage14d,
		priceChangePercentage30d = this.priceChangePercentage30d,
		priceChangePercentage60d = this.priceChangePercentage60d,
		priceChangePercentage200d = this.priceChangePercentage200d,
		priceChangePercentage1y = this.priceChangePercentage1y,
		lastUpdated = ZonedDateTime.parse(this.lastUpdated)
	)
}