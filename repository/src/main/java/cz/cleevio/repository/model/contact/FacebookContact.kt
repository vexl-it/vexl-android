package cz.cleevio.repository.model.contact

import android.net.Uri
import cz.cleevio.network.response.contact.FacebookUserResponse

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