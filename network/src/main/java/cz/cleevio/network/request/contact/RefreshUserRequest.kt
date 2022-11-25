package cz.cleevio.network.request.contact

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RefreshUserRequest constructor(
	val offersAlive: Boolean
)
