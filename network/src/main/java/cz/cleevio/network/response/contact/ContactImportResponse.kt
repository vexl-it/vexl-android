package cz.cleevio.network.response.contact

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContactImportResponse constructor(
	val imported: Boolean,
	val message: String
)

