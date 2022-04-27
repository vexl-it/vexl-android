package cz.cleevio.cache.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MyOfferEntity(
	@PrimaryKey(autoGenerate = true)
	val id: Long = 0,
	val extId: String,
	val privateKey: String,
	val publicKey: String
)
