package cz.cleevio.cache.entity

import androidx.room.Embedded
import androidx.room.Relation

data class OfferWithLocations(
	@Embedded val offer: OfferEntity,
	@Relation(
		parentColumn = "id",
		entityColumn = "offerId"
	)
	val locations: List<LocationEntity>
)