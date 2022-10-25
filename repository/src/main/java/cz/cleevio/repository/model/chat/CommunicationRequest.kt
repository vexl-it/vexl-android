package cz.cleevio.repository.model.chat

import android.os.Parcelable
import cz.cleevio.repository.model.contact.ContactLevel
import cz.cleevio.repository.model.group.Group
import cz.cleevio.repository.model.offer.Offer
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommunicationRequest constructor(
	val message: ChatMessage,
	val offer: Offer,
	val group: Group? = null,
	//need to display correct FriendLevel in UI
	val contactLevels: List<ContactLevel> = listOf()
) : Parcelable
