package cz.cleevio.cache.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.ZonedDateTime

// TODO Strings to enums
@Entity(indices = [Index(value = ["externalOfferId"], unique = true)])
data class OfferEntity(
	@PrimaryKey(autoGenerate = true)
	val offerId: Long = 0,
	val externalOfferId: String,
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
	val offerType: String,
	val activePriceState: String,
	val activePriceValue: BigDecimal,
	val active: Boolean,
	val groupUuid: String,
	val commonFriends: String,
	val createdAt: ZonedDateTime,
	val modifiedAt: ZonedDateTime,
	//custom flags
	val isMine: Boolean,
	val isRequested: Boolean,
)