package cz.cleevio.cache.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class OfferWithLocationsAndCommonFriends(

	@Embedded
	val offer: OfferEntity, // Main table

	@Relation(
		parentColumn = "offerId", // column name in offer (parent) table
		entityColumn = "offerId" // column name in location table, which references the parent column
	)
	val locations: List<LocationEntity>, // One to many relation with location table

	@Relation(
		parentColumn = "offerId", // column name in offer table
		entityColumn = "contactId", // column name in contact table
		associateBy = Junction(OfferCommonFriendCrossRef::class)
	)
	val commonFriends: List<ContactEntity> // Many to many relation with Contact table (common friends)
)