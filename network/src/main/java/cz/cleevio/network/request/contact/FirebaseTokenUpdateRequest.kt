package cz.cleevio.network.request.contact

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FirebaseTokenUpdateRequest constructor(
	val firebaseToken: String
)
