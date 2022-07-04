package cz.cleevio.cache.entity

import androidx.room.Entity

@Entity(primaryKeys = ["offerId", "contactId"])
data class OfferCommonFriendCrossRef(
	val offerId: Long,
	val contactId: Long
)