package cz.cleevio.network.request.contact

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NewContactRequest constructor(
	val contacts: List<String>
)