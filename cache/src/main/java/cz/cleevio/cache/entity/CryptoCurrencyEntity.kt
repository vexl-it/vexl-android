package cz.cleevio.cache.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity
data class CryptoCurrencyEntity(
	@PrimaryKey
	val id: Long,
	val priceUsd: BigDecimal,
	val priceCzk: BigDecimal,
	val priceEur: BigDecimal,
)