package cz.cleevio.cache.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.ZonedDateTime

// TODO Strings to enums
@Entity
data class OfferEntity(
	@PrimaryKey(autoGenerate = true)
	val id: Long = 0,
	val offerId: String,
	val location: String,
	val userPublicKey: String,
	val offerPublicKey: String,
	val offerDescription: String,
	val amountBottomLimit: BigDecimal,
	val amountTopLimit: BigDecimal,
	val feeState: String,
	val feeAmount: BigDecimal,
	val locationState: String,
	val paymentMethod: String,
	val btcNetwork: String,
	val friendLevel: String,
	val createdAt: ZonedDateTime,
	val modifiedAt: ZonedDateTime
)