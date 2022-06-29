package cz.cleevio.repository.model.contact

import android.content.Context
import android.net.Uri
import cz.cleevio.cache.entity.FacebookContactEntity
import cz.cleevio.network.response.contact.FacebookUserResponse
import cz.cleevio.repository.R
import kotlinx.parcelize.Parcelize

@Suppress("DataClassShouldBeImmutable")
@Parcelize
data class FacebookContact(
	override val id: String,
	override val name: String,
	override val photoUri: Uri?,
	override var markedForUpload: Boolean = true
) : BaseContact(
	id = id,
	name = name,
	photoUri = photoUri,
	markedForUpload = markedForUpload
) {
	override fun getIdentifier(): String = id
	override fun getHashedContact(): String {
		TODO("Not yet implemented")
	}

	override fun getChatDescription(context: Context): String =
		context.resources.getString(R.string.chat_common_friends_facebook_contact)
}

fun FacebookUserResponse.fromFacebook(): FacebookContact {
	return FacebookContact(
		id = this.id,
		name = this.name,
		photoUri = this.picture?.data?.url?.let {
			Uri.parse(it)
		}
	)
}

fun FacebookContactEntity.fromCache(): FacebookContact {
	return FacebookContact(
		id = this.facebookId,
		name = this.name,
		photoUri = this.photoUri?.let {
			Uri.parse(it)
		}
	)
}