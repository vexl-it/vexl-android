package cz.cleevio.network.request.contact

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeleteContactRequest constructor(
	val contactsToDelete: List<String>
)