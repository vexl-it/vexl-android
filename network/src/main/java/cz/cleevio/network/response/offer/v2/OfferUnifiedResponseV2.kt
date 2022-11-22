package cz.cleevio.network.response.offer.v2

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OfferUnifiedResponseV2 constructor(
	val id: Long,
	val offerId: String,
	val publicPayload: String,
	val privatePayload: String,
	@Deprecated("BE is now handling expiration by refreshed_at field")
	val expiration: Long,
	val createdAt: String,
	val modifiedAt: String,
)
