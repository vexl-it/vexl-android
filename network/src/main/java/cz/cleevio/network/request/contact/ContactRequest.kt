package cz.cleevio.network.request.contact

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContactRequest constructor(
	val contacts: List<String>
)
