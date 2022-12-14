package cz.cleevio.cache.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.ZonedDateTime

@Entity(indices = [Index(value = ["externalOfferId"], unique = true)])
data class OfferEntity(
	@PrimaryKey(autoGenerate = true)
	val offerId: Long = 0,
	// id field should be used only for ordering offers
	@ColumnInfo(defaultValue = "-1")
	val id: Long,
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
	val activePriceCurrency: String,
	val active: Boolean,
	val groupUuid: String,
	val currency: String,
	val commonFriends: String,
	val createdAt: ZonedDateTime,
	val modifiedAt: ZonedDateTime,
	//custom flags
	val isMine: Boolean,
	val isRequested: Boolean,
)