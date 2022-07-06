package cz.cleevio.repository.model.contact

import android.content.Context
import android.net.Uri
import cz.cleevio.cache.entity.ContactEntity
import cz.cleevio.network.response.contact.FacebookUserResponse
import cz.cleevio.repository.R
import kotlinx.parcelize.Parcelize

@Suppress("DataClassShouldBeImmutable")
@Parcelize
data class FacebookContact(
	override val id: Long,
	override val name: String,
	override val photoUri: Uri?,
	override var markedForUpload: Boolean = true,
	val facebookId: String,
	val hashedFacebookId: String = ""
) : BaseContact(
	id = id,
	name = name,
	photoUri = photoUri,
	markedForUpload = markedForUpload
) {
	override fun getIdentifier(): String = facebookId
	override fun getHashedContact(): String = hashedFacebookId

	override fun getChatDescription(context: Context): String =
		context.resources.getString(R.string.chat_common_friends_facebook_contact)

	override fun getContactType(): String =
		"FACEBOOK"
}

fun FacebookUserResponse.fromFacebook(): FacebookContact {
	return FacebookContact(
		id = 0L, // we don't have it at this moment
		facebookId = this.id,
		name = this.name,
		photoUri = this.picture?.data?.url?.let {
			Uri.parse(it)
		}
	)
}

fun FacebookContact.toDao(): ContactEntity = ContactEntity(
	contactId = this.id,
	contactType = this.getContactType(),
	name = this.name,
	facebookId = this.facebookId,
	facebookIdHashed = this.hashedFacebookId,
	photoUri = this.photoUri.toString()
)