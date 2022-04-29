package cz.cleevio.cache.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(
	foreignKeys = [
		ForeignKey(
			onDelete = ForeignKey.CASCADE,
			entity = OfferEntity::class,
			parentColumns = ["id"],
			childColumns = ["offerId"]
		)
	]
)
data class LocationEntity(
	@PrimaryKey(autoGenerate = true)
	val id: Long = 0,
	val offerId: Long,
	val longitude: BigDecimal,
	val latitude: BigDecimal,
	val radius: BigDecimal
)