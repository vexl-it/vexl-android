package cz.cleevio.network.response.contact

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContactNotImportResponse constructor(
	val newContacts: List<String>
)
