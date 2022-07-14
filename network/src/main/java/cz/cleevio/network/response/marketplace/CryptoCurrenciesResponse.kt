package cz.cleevio.network.response.marketplace

import com.squareup.moshi.JsonClass
import java.math.BigDecimal

@JsonClass(generateAdapter = true)
data class CryptoCurrenciesResponse(
	val priceUsd: BigDecimal,
	val priceCzk: BigDecimal,
	val priceEur: BigDecimal,
	val priceChangePercentage24h: BigDecimal,
	val priceChangePercentage7d: BigDecimal,
	val priceChangePercentage14d: BigDecimal,
	val priceChangePercentage30d: BigDecimal,
	val priceChangePercentage60d: BigDecimal,
	val priceChangePercentage200d: BigDecimal,
	val priceChangePercentage1y: BigDecimal,
	val lastUpdated: String
)