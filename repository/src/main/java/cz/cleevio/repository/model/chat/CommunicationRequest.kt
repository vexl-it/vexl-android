package cz.cleevio.repository.model.chat

import android.os.Parcelable
import cz.cleevio.repository.model.group.Group
import cz.cleevio.repository.model.offer.Offer
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommunicationRequest constructor(
	val message: ChatMessage,
	val offer: Offer? = null,
	val group: Group? = null,
	//Fixme: maybe user later?
) : Parcelable
