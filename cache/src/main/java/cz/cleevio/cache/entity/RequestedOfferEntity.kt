package cz.cleevio.cache.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["offerId"], unique = true)])
data class RequestedOfferEntity constructor(
	@PrimaryKey(autoGenerate = true)
	val id: Long = 0,
	val offerId: String,
)
